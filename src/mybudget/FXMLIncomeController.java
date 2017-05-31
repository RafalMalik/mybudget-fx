/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mybudget;

import entity.Income;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.hibernate.Session;

/**
 * FXML Controller class
 *
 * @author Malina
 */
public class FXMLIncomeController implements Initializable {

    @FXML
    private TextField amount;

    @FXML
    private TextField title;

    @FXML
    private TextArea description;
    
    @FXML
    private Label error;

    private HibernateUtil db;

    public String getAmount() {
        return amount.getText();
    }

    public void setAmount(String amount) {
        this.amount.setText(amount);
    }

    public String getTitle() {
        return title.getText();
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public String getDescription() {
        return description.getText();
    }

    public void setDescription(String description) {
        this.description.setText(description);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void handleSubmitAction(ActionEvent event) throws IOException {
                
        if (!this.validate()) {
            error.setText("Popraw formularz!");
            return;
        }

        Income income = new Income();
        income.setAmount(Double.parseDouble(this.getAmount()));
        income.setTitle(this.getTitle());
        income.setDescription(this.getDescription());
        income.setDate(new Date());
        
        Session session = db.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        session.save(income);
        session.getTransaction().commit();
        
        error.setText("Dochód został poprawnie zapisany.");
        this.clearFields();
    }

    private boolean validate() {
        error.setText("");
        
        if (this.getTitle().trim().isEmpty()) {
            return false;
        }

        try {
            Double amount = Double.parseDouble(this.getAmount());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void clearFields() {
        this.setAmount("");
        this.setTitle("");
        this.setDescription("");
    }
}
