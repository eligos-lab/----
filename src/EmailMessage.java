import java.util.Date;

public class EmailMessage {
    private String id;
    private String from;
    private String subject;
    private String body;
    private Date date;

    public EmailMessage(String id, String from, String subject, String body, Date date) {
        this.id = id;
        this.from = from;
        this.subject = subject;
        this.body = body;
        this.date = date;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    @Override
    public String toString() {
        return String.format("From: %s | Subject: %s | Date: %s", from, subject, date);
    }
}