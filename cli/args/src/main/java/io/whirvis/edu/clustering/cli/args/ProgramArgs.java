/*
 * the MIT License (MIT)
 *
 * Copyright (c) 2023 Trent Summerlin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * the above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.whirvis.edu.clustering.cli.args;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A parser for program arguments.
 *
 * @see ProgramParam
 * @see #add(int, ProgramParam)
 * @see #parse(String[])
 */
public final class ProgramArgs {

    private final List<ProgramParam<?>> params;
    private final Map<ProgramParam<?>, Object> args;
    private final ReadWriteLock parseLock;
    private ParseState parseState;

    /**
     * Constructs a new {@code ProgramArgs} instance.
     */
    public ProgramArgs() {
        this.params = new ArrayList<>();
        this.args = new HashMap<>();
        this.parseLock = new ReentrantReadWriteLock();
        this.parseState = ParseState.NOT_PARSED;
    }

    /**
     * Returns the usage for these program arguments, based on the current
     * parameters. If there are no parameters, an empty string is returned.
     * If no parameter is set for an index, {@code "<?>"} will be inserted
     * in its place.
     *
     * @return the usage for these program arguments.
     * @see #add(int, ProgramParam)
     */
    public String getUsage() {
        StringBuilder usage = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            ProgramParam<?> param = params.get(i);
            usage.append(param != null ? param : "<?>");
            usage.append(i + 1 < params.size() ? " " : "");
        }
        return usage.toString();
    }

    /**
     * Returns the amount of parameters.
     *
     * @return the amount of parameters.
     */
    public int getParamCount() {
        return params.size();
    }

    private void expectParseState(ParseState expectedState) {
        parseLock.readLock().lock();
        try {
            if (parseState == ParseState.NOT_PARSED
                    && expectedState == ParseState.PARSED) {
                throw new IllegalStateException("Arguments not parsed");
            } else if (parseState == ParseState.PARSED
                    && expectedState == ParseState.NOT_PARSED) {
                throw new IllegalStateException("Arguments already parsed");
            }
        } finally {
            parseLock.readLock().unlock();
        }
    }

    private void parseParam(ProgramParam<?> param, String argText) {
        if (param == null) {
            return; /* parameter wasn't specified */
        }
        Object arg = param.parse(argText);
        args.put(param, arg);
    }

    /**
     * Parses the given program arguments for the previously specified
     * parameters.
     *
     * @param args the program arguments.
     * @return this instance.
     * @throws NullPointerException      if {@code args} or any of its elements
     *                                   are {@code null}.
     * @throws IndexOutOfBoundsException if {@code args} does not have a length
     *                                   great enough for the parameter with the
     *                                   highest index.
     * @throws IllegalStateException     if no parameters have been specified;
     *                                   if these program arguments have already
     *                                   been parsed.
     * @see #add(int, ProgramParam)
     */
    public ProgramArgs parse(String[] args) {
        requireNonNullArray(args, "args");

        if (params.size() > args.length) {
            String msg = "Highest parameter index " + (params.size() - 1)
                    + " too high for argument count of " + args.length;
            throw new IndexOutOfBoundsException(msg);
        }

        parseLock.writeLock().lock();
        try {
            /*
             * It would not make sense to parse arguments when no parameters
             * have been specified. As such, assume this was a mistake by the
             * user and throw an exception.
             */
            if (params.isEmpty()) {
                String msg = "No specified parameters (nothing to parse)";
                throw new IllegalStateException(msg);
            }

            /*
             * Only allow users to parse arguments with an instance one time.
             * Allowing them to do so multiple times could lead to confusing
             * situations with parameters suddenly having different values.
             */
            this.expectParseState(ParseState.NOT_PARSED);

            for (int i = 0; i < params.size(); i++) {
                ProgramParam<?> param = params.get(i);
                this.parseParam(param, args[i]);
            }

            this.parseState = ParseState.PARSED;
        } finally {
            parseLock.writeLock().unlock();
        }

        return this;
    }

    /**
     * Adds a parameter for parsing.
     * <p>
     * After calling {@link #parse(String[])}, the argument for the parameter
     * can be retrieved via {@link #get(ProgramParam)}.
     *
     * @param index the index of the argument. This corresponds to where the
     *              argument resides in the array.
     * @param param the parameter being added.
     * @throws IndexOutOfBoundsException if {@code index} is negative.
     * @throws NullPointerException      if {@code param} is {@code null}.
     * @throws IllegalStateException     if {@code param} has already been
     *                                   added;
     *                                   if these program arguments have
     *                                   already been parsed.
     */
    public void add(int index, ProgramParam<?> param) {
        if (index < 0) {
            String msg = "index cannot be negative";
            throw new IndexOutOfBoundsException(msg);
        }

        Objects.requireNonNull(param, "param cannot be null");
        if (params.contains(param)) {
            String msg = "param already added";
            throw new IllegalStateException(msg);
        }

        /*
         * It would not make sense to add parameters after already parsing
         * program arguments. As such, assume this was a mistake by the user
         * and have an exception thrown.
         */
        this.expectParseState(ParseState.NOT_PARSED);

        parseLock.writeLock().lock();
        try {
            resizeForIndex(index, params);
            params.add(index, param);
        } finally {
            parseLock.writeLock().unlock();
        }
    }

    /**
     * Returns the argument for a parameter.
     *
     * @param param the parameter whose argument to get.
     * @param <T>   the argument type of the parameter.
     * @return the parameter's argument.
     * @throws NullPointerException  if {@code param} is {@code null}.
     * @throws IllegalStateException if these program arguments have not yet
     *                               been parsed.
     * @throws ParamException        if {@code param} was not previously added
     *                               (i.e., not parsed and has no value).
     */
    @SuppressWarnings("unchecked")
    public <T> T get(ProgramParam<T> param) {
        Objects.requireNonNull(param, "param cannot be null");

        /*
         * The arguments for parameters are guaranteed to be null before
         * parsing them. As such, assume this was a mistake by the user
         * and have an exception thrown.
         */
        this.expectParseState(ParseState.PARSED);

        parseLock.readLock().lock();
        try {
            Object arg = args.get(param);
            if (arg == null) {
                throw new ParamException("No such parameter");
            }
            return (T) arg;
        } finally {
            parseLock.readLock().unlock();
        }
    }

    private enum ParseState {
        NOT_PARSED, PARSED
    }

    /**
     * Resizes the given list to ensure it can hold elements up to the
     * given index. If the list needs no resizing, then this method is
     * a no-op.
     *
     * @param index the index to resize the list for.
     * @param list  the list to resize, if necessary.
     */
    public static void resizeForIndex(int index, List<?> list) {
        if (index > list.size()) {
            for (int i = list.size(); i < index; i++) {
                list.add(i, null);
            }
        }
    }

    /**
     * Requires that an array and all of its elements not be {@code null}.
     * <p>
     * The given object may be a multidimensional array, or even just a
     * single value. This is done via recursion, with this method calling
     * itself each time it encounters an element that is of an array type.
     *
     * @param array the array to check for non-nullity.
     * @param name  what to call the array in error messages.
     * @throws NullPointerException if {@code array} or any of its elements
     *                              are {@code null}.
     */
    public static void requireNonNullArray(Object array, String name) {
        Objects.requireNonNull(array, name + " cannot be null");
        if (!array.getClass().isArray()) {
            return; /* nothing else to check */
        }
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object elem = Array.get(array, i);
            Objects.requireNonNull(elem, name + " cannot contain null");
            requireNonNullArray(elem, name);
        }
    }

}
