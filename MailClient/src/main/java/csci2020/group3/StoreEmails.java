package csci2020.group3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageNumberTerm;

public class StoreEmails {
    private static Folder folder;
    static FileWriter writeFileCurrent;

    public static void storeEmails(String email_addr, String pwd) {

        Properties props = System.getProperties();
        //props.setProperty("mail.store.protocol", "imaps");
        props.put("mail.imap.fetchsize", "819200");
        props.setProperty("mail.imap.partialfetch", "false");
        props.setProperty("mail.imaps.partialfetch", "false");
        try {

            // Initializing session and store var
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", email_addr, pwd);

            // Loads specified folder and prints total number of emails inside
            folder = store.getFolder("INBOX");
            System.out.println("Total # of emails: " + folder.getMessageCount());

            System.out.println("Total # of unread emails: " + folder.getUnreadMessageCount());

            // Opening store folder
            folder.open(Folder.READ_ONLY);
            //folder.open(Folder.)

            // Reads read messages
            Message read_messages[] = folder.search(new FlagTerm(new Flags(Flag.SEEN), true));

            // Reads unread messages
            Message unread_messages[] = folder.search(new FlagTerm(new Flags(Flag.SEEN), false));

            // Setting up FetchProfile
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.CONTENT_INFO);
            folder.fetch(read_messages, fp);

            // Creating an array of Emails to store each email
            Email[] emails = new Email[folder.getMessageCount()];

            try {

                long startTime = System.nanoTime();

                printFolder(read_messages, emails);

                // If unread messages exist, it reads them and places them in their respective email position
                if (unread_messages.length != 0) {
                    System.out.println("Writing unread messages...");
                    folder.fetch(unread_messages, fp);
                    printFolder(unread_messages, emails);
                }

                // Temporarily setup to test different storing methods and their speeds
                long endTime = System.nanoTime();
                long timeElapsed = (endTime - startTime)/1000000000;
                System.out.println("Time taken to load files: " + timeElapsed + "s");

                folder.close(true);
                store.close();
            } catch (Exception ex) {
                System.out.println("Reading mail exception");
                ex.printStackTrace();
            }
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int count;
    // Outputs all emails from desired gmail folder
    public static void printFolder(Message[] msgs, Email[] emails) throws Exception {

        count = msgs.length;
        for (int i = msgs.length - 1; i >= 0; i--) {

            // Creating Email Class to store key components
            Email email = new Email();

            System.out.println("Reading Email #" + (i + 1) + ".....");
            //writeFile.write("Email #" + (i + 1) + ":");
            email.setId(i+1);

            printEmail(msgs[i], email);

            // Copying current email to emails list
            emails[msgs[i].getMessageNumber()-1] = email;

            count--;
        }
        //writeFile.close();

        // Creating json data file with all emails
        File jsonFile = new File("src/data/emails.json");
        jsonFile.getParentFile().mkdirs();
        try (Writer jsonOutput = new FileWriter(jsonFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(emails, jsonOutput);
        }
    }

    // Outputs Email
    public static void printEmail(Message message, Email email) throws Exception {
        Address[] a;


        // Getting email sender
        if ((a = message.getFrom()) != null) {
            for (int j = 0; j < a.length; j++) {
                email.setFrom(a[j].toString());
            }
        }

        // Getting email recipient - might need
        /*
        if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++) {

            }
        }
        */

        email.setSubject(message.getSubject());
        email.setDate(message.getReceivedDate());

        getContent(message, email);

    }

    public static void getContent(Message msg, Email email) {
        try {

            //String contentType = msg.getContentType();

            //writeFile.write("Content Type: " + contentType);

            // Checking if mimetype is a multipart
            if (msg.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) msg.getContent();
                int count = mp.getCount();
                for (int i = 0; i < count; i++) {

                    //System.out.println(mp.getBodyPart(i).getContentType());

                    if (mp.getBodyPart(i).isMimeType("text/*")) {
                        writeEmail(mp.getBodyPart(i), email);
                    }
                }
            }

            // Condition to handle text/plain emails
            else if (msg.isMimeType("text/plain")) {

                // Creating text file to store current text content
                try {
                    File curr_file = new File("src/data/email-" + count + ".html");
                    curr_file.getParentFile().mkdirs();
                    writeFileCurrent = new FileWriter(curr_file);
                    email.setContentPath("src/data/email-" + count + ".html");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String tp = msg.getContent().toString();

                //writeFile.write(tp);
                writeFileCurrent.write(tp);
            }

            // writes everything in the buffer to disk without having to close
            //System.out.println(msg.getContentType());
            writeFileCurrent.flush();

            // getContent Exception
        } catch (Exception ex) {
            System.out.println("getContent exception");
            ex.printStackTrace();
        }
    }

    public static void writeEmail(Part p, Email email) throws Exception {

        // Creating html file to store current html content
        try {
            File curr_file = new File("src/data/email-" + count + ".html");
            curr_file.getParentFile().mkdirs();
            writeFileCurrent = new FileWriter(curr_file);
            email.setContentPath("src/data/email-" + count + ".html");
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream is = p.getInputStream();

        if (!(is instanceof BufferedInputStream)) {
            is = new BufferedInputStream(is);
        }
        int w;
        while ((w = is.read()) != -1) {
            //FileWriter writeFile = new FileWriter("emails.txt");
            //writeFile.write(w);

            writeFileCurrent.write(w);

        }
    }

}
