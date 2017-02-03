package mil.emp3.publisher;

public class TextViewLogger {

    private MainActivity activity = null;

    public TextViewLogger(MainActivity activity) {
        this.activity = activity;
    }


    public synchronized void log(String message) {
        activity.runOnUiThread(new LogRunner(message));
    }


    public synchronized void clear() {
        activity.runOnUiThread(new LogRunner());
    }


    private class LogRunner implements Runnable {
        private String message = null;

        public LogRunner() {
        }

        public LogRunner(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            if (message==null) TextViewLogger.this.activity.getTextView().setText("");
            else TextViewLogger.this.activity.getTextView().append("\n"+message);
            if (TextViewLogger.this.activity.getScrollView()!=null)
                TextViewLogger.this.activity.getScrollView().scrollTo(0,TextViewLogger.this.activity.getTextView().getHeight());
        }
    }

}
