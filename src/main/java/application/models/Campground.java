package application.models;

import javax.persistence.*;
import java.util.Collection;

@Entity
public class Campground {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String image;
    @Lob
    private String description;
    private double price;
    @OneToMany(mappedBy = "campground", cascade = CascadeType.ALL)
    private Collection<Comment> comments;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User author;

    public Campground() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public Collection<Comment> getComments() {
        return comments;
    }

    public User getAuthor() { return author; }

    public void setId(Long id) { this.id = id; }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setComments(Collection<Comment> comments) { this.comments = comments; }

    public void setAuthor(User author) { this.author = author; }

    public boolean checkOwnership(String authorName) {
        return author.getUsername().equals(authorName);
    }

}
