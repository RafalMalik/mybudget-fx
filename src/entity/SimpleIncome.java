package entity;
import java.time.LocalDate;
import javafx.beans.property.DoubleProperty;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Model class for a Person.
 *
 * @author Marco Jakob
 */

public class SimpleIncome {

    private int id;

    private final StringProperty title;
    
    private final StringProperty description;
    
    //private final ObjectProperty<LocalDate> date;
    
    private final DoubleProperty amount;

    /**
     * Default constructor.
     */
    public SimpleIncome() {
        this(null, null, null);
    }

   
    public SimpleIncome(String title, String description, Double amount) {
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public Double getAmount() {
        return amount.get();
    }

    public void setAmount(Double amount) {
        this.amount.set(amount);
    }

    public DoubleProperty amountProperty() {
        return amount;
    }
    
    
}