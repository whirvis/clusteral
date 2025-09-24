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

/**
 * Signals an error relating to {@link ProgramParam}.
 */
@SuppressWarnings("unused")
public class ParamException extends RuntimeException {

    private static final long serialVersionUID = -6746136059375934439L;

    private transient ProgramParam<?> culprit;

    /**
     * Constructs a new {@code ParamException} with the specified detail
     * message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i>
     * automatically incorporated in this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method). A {@code null}
     *                value is permitted, and indicates that the cause is
     *                nonexistent or unknown.
     */
    public ParamException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code ParamException} with the specified detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     */
    public ParamException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ParamException} with the specified cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i>
     * automatically incorporated in this exception's detail message.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method). A {@code null}
     *              value is permitted, and indicates that the cause is
     *              nonexistent or unknown.
     */
    public ParamException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code ParamException} with no detail message.
     */
    public ParamException() {
        super();
    }

    /**
     * Sets the parameter responsible for this exception.
     *
     * @param culprit the parameter response for this exception.
     * @return this exception.
     */
    /* package-private */
    final ParamException setCulprit(ProgramParam<?> culprit) {
        this.culprit = culprit;
        return this;
    }

    /**
     * Returns the parameter which caused this error.
     *
     * @return the parameter which caused this error, {@code null} if this
     * exception was not thrown in the context of a {@code ProgramParam} thus
     * leaving it unset.
     */
    public final ProgramParam<?> getCulprit() {
        return this.culprit;
    }

    /**
     * Returns {@code culprit.toString()} if the culprit is known,
     * otherwise returns {@code "<?>"} in its place.
     *
     * @return {@code culprit.toString()} if the culprit is known,
     * {@code "<?>"} otherwise.
     */
    public final String getCulpritStr() {
        return culprit != null ? culprit.toString() : "<?>";
    }

}
