package csci2020.group3;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;

public class loadThread implements Runnable {

    private String email_addr;
    private String pwd;
    private ListView<EmailListView.EmailList> emailList;
    private WebView wb;
    private String[] mailboxList = { "INBOX", "[Gmail]/Sent Mail", "[Gmail]/Trash", "[Gmail]/Spam", "[Gmail]/Starred"};
    private TextField searchField;

    // Thread Constructor
    public loadThread(String email_addr, String pwd, ListView<EmailListView.EmailList> emailList, WebView wb, TextField searchField) {
        this.email_addr = email_addr;
        this.pwd = pwd;
        this.emailList = emailList;
        this.wb = wb;
        this.searchField = searchField;
    }

    public void run() {

        long startTime = System.nanoTime();

        // each open IMAPFolder gets a single connection to the server so concurrency is limited
        for (int i = 0; i < 5; i++) {
            StoreEmails.storeEmails(this.email_addr, this.pwd, mailboxList[i]);
        }

        long endTime = System.nanoTime();
        long timeElapsed = (endTime - startTime)/1000000000;
        System.out.println("Time taken to load files: " + timeElapsed + "s");

        try {
            // Loads the users INBOX into the emailList by default
            LoadEmailListView.loadData(emailList, wb, "INBOX", searchField);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
