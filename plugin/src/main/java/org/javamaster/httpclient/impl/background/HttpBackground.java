package org.javamaster.httpclient.impl.background;

import consulo.application.Application;
import consulo.application.ReadAction;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author yudong
 */
public class HttpBackground<T> {
    private Consumer<T> resultConsumer;
    private Consumer<Exception> exceptionConsumer;

    public HttpBackground<T> finishOnUiThread(Consumer<T> resultConsumer) {
        this.resultConsumer = resultConsumer;
        return this;
    }

    public HttpBackground<T> exceptionallyOnUiThread(Consumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }

    public static <T> HttpBackground<T> runInBackgroundReadActionAsync(Supplier<T> supplier) {
        HttpBackground<T> d = new HttpBackground<>();

        Application application = Application.get();
        application.executeOnPooledThread(() -> {
            ReadAction.run(() -> {
                try {
                    T result = supplier.get();

                    Application.get().invokeLater(() -> {
                        try {
                            if (d.resultConsumer != null) {
                                d.resultConsumer.accept(result);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            if (d.exceptionConsumer != null) {
                                d.exceptionConsumer.accept(ex);
                            }
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();

                    Application.get().invokeLater(() -> {
                        if (d.exceptionConsumer != null) {
                            d.exceptionConsumer.accept(ex);
                        }
                    });
                }
            });
        });

        return d;
    }
}
