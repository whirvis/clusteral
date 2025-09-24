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

import java.util.Locale;

/**
 * A parameter which takes in a {@code boolean}.
 */
@SuppressWarnings("unused")
public final class BooleanParam extends ProgramParam<Boolean> {

    /**
     * Constructs a new {@code BooleanParam}.
     *
     * @param displayName the display name for this parameter.
     * @throws NullPointerException if {@code displayName} is {@code null}.
     */
    public BooleanParam(String displayName) {
        super(displayName, null);
    }

    @Override
    protected Boolean decode(String text) {
        String lowercase = text.toLowerCase(Locale.ROOT);
        if (lowercase.equals("true")) {
            return true;
        } else if (lowercase.equals("false")) {
            return false;
        } else {
            String msg = "Invalid input for boolean (" + text + ")";
            throw new ParamException(msg);
        }
    }

}
