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

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A parameter for the program.
 * <p>
 * These allow for parameters to be easily defined in code and then their
 * arguments parsed to the desired type. Furthermore, they also act as a key
 * for retrieving parameter arguments.
 *
 * @param <T> the argument type.
 * @see ProgramArgs#get(ProgramParam)
 */
@SuppressWarnings("unused")
public abstract class ProgramParam<T> {

    private final String displayName;
    private final Consumer<T> validator;

    /**
     * Constructs a new {@code ProgramParam}.
     *
     * @param displayName the display name for this parameter.
     * @param validator   an additional, optional, validator for the argument
     *                    of this parameter. These can be used to ensure that
     *                    an argument meets certain criteria.
     * @throws NullPointerException if {@code displayName} is {@code null}.
     */
    public ProgramParam(
            String displayName,
            Consumer<T> validator) {
        this.displayName = Objects.requireNonNull(displayName,
                "displayName cannot be null");
        this.validator = validator;
    }

    /**
     * Returns the display name of this parameter.
     *
     * @return the display name of this parameter.
     */
    public final String getDisplayName() {
        return this.displayName;
    }

    /**
     * Decodes the text supplied for this parameter to the argument type.
     *
     * @param text the argument for the parameter.
     * @return the decoded parameter.
     */
    protected abstract T decode(String text);

    private T decodeText(String text) {
        if (text.matches(REGEX_EMPTY_STR)) {
            throw new IllegalArgumentException("Empty argument");
        }
        try {
            return this.decode(text);
        } catch (ParamException e) {
            throw e.setCulprit(this); /* don't needlessly wrap */
        } catch (RuntimeException e) {
            throw culpritException(this, "Decode failure", e);
        }
    }

    private T validateArgument(T value) {
        try {
            if (validator != null) {
                validator.accept(value);
            }
            return value;
        } catch (ParamException e) {
            throw e.setCulprit(this); /* don't needlessly wrap */
        } catch (RuntimeException e) {
            throw culpritException(this, "Validation failure", e);
        }
    }

    /**
     * Decodes and validates the given text.
     * <p>
     * "Decoding" is the process of parsing the text into a value.
     * "Validation" is the process of ensuring a value is valid for
     * a parameter (e.g., requiring a positive integer).
     *
     * @param text the argument text to parse.
     * @return the parsed value.
     * @throws ParamException if a decoding or validation error occurs.
     */
    /* package-private */
    final T parse(String text) {
        T value = this.decodeText(text);
        return this.validateArgument(value);
    }

    @Override
    public String toString() {
        return "<" + displayName + ">";
    }

    private static final String REGEX_EMPTY_STR = "^$";

    /**
     * Constructs a new {@code ParamException} with the specified detail
     * message and cause before setting the culprit to the given parameter.
     *
     * @param param   the parameter response for this exception.
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method). A {@code null}
     *                value is permitted, and indicates that the cause is
     *                nonexistent or unknown.
     * @return the exception to throw.
     * @see ParamException#setCulprit(ProgramParam)
     */
    private static ParamException culpritException(
            ProgramParam<?> param,
            String message,
            Throwable cause) {
        ParamException e = new ParamException(message, cause);
        return e.setCulprit(param);
    }

}
