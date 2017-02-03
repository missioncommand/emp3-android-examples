package mil.emp3.examples.common;

public interface UserAction {
    void actOn(String userAction);
    String[] getSupportedUserActions(); // Actions shown in specific buttons, max six
    String[] getMoreActions();          // Additional actions
}
