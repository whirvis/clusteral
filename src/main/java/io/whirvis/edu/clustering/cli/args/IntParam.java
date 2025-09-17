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

import java.util.function.Consumer;

/**
 * A parameter which takes in an {@code int}.
 */
@SuppressWarnings("unused")
public final class IntParam extends ProgramParam<Integer> {

    /**
     * Constructs a new {@code IntParam}.
     *
     * @param displayName the display name for this parameter.
     * @param validator   an additional, optional, validator for the argument
     *                    of this parameter. These can be used to ensure that
     *                    an argument meets certain criteria.
     * @throws NullPointerException if {@code displayName} is {@code null}.
     */
    public IntParam(
            String displayName,
            Consumer<Integer> validator) {
        super(displayName, validator);
    }

    /**
     * Constructs a new {@code IntParam}.
     *
     * @param displayName the display name for this parameter.
     * @throws NullPointerException if {@code displayName} is {@code null}.
     */
    public IntParam(String displayName) {
        super(displayName, null);
    }

    @Override
    protected Integer decode(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            String msg = "Invalid input for int (" + text + ")";
            throw new ParamException(msg);
        }
    }

}
