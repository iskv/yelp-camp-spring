package application.models;

import javax.persistence.*;
import java.sql.Date;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String text;
    private Date date;
    @ManyToOne
    @JoinColumn(name = "campground_id")
    private Campground campground;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User author;

    public Comment() {}

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public Campground getCampground() {
        return campground;
    }

    public User getAuthor() { return author; }

    public void setId(Long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCampground(Campground camp) {
        this.campground = camp;
    }

    public void setAuthor(User author) { this.author = author; }

    public long getLifetime() {
        return ( System.currentTimeMillis() - date.getTime() ) / 86_400_000;
    }

    public void setCurrentDate() {
        setDate(new Date(System.currentTimeMillis()));
    }

    public boolean checkOwnership(String authorName) {
        return author.getUsername().equals(authorName);
    }

}
