package mil.emp3.examples.capabilities.common;

public interface ITestMenuManager {
    void recreateTestMenu(String []supportedUserActions, String[] moreUserActions);
    int getMaxSupportedActions();
}
