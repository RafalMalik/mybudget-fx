/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mybudget;

import entity.Expense;
import entity.Income;
import entity.SimpleIncome;
import entity.Status;
import java.io.IOException;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 *
 * @author Malina
 */
public class FXMLMyBudgetController implements Initializable {

    @FXML
    private Label label;

    @FXML
    private Button newIncome;

    private HibernateUtil db;

    @FXML
    private TableView<SimpleIncome> tableIncome;

    @FXML
    private TableColumn<SimpleIncome, String> columnTitleIncome;

    @FXML
    private TableColumn<SimpleIncome, String> columnDescriptionIncome;

    @FXML
    private TableColumn<SimpleIncome, Double> columnAmountIncome;

    @FXML
    private TableView<SimpleIncome> tableExpense;

    @FXML
    private TableColumn<SimpleIncome, String> columnTitleExpense;

    @FXML
    private TableColumn<SimpleIncome, String> columnDescriptionExpense;

    @FXML
    private TableColumn<SimpleIncome, Double> columnAmountExpense;

    @FXML
    private Label settlementPeriod;

    @FXML
    private Label balance;

    @FXML
    private Double accountBalance;

    public ObservableList<SimpleIncome> incomes = FXCollections.observableArrayList();
    public ObservableList<SimpleIncome> expenses = FXCollections.observableArrayList();

    @FXML
    private void resetBudgetAction(ActionEvent event) throws IOException {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("UWAGA!");
        alert.setHeaderText("Czy na pewno chcesz wyczyścić statystyki");
        alert.setContentText("Tej operacji nie da się cofnąć!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            
            Session session = db.getSessionFactory().openSession();
               
            session.beginTransaction();
        
            List<Expense> expenseList = session.
                    createCriteria(Expense.class).list();
            for (Expense expense : expenseList) {
                session.delete(expense);
            }

            List<Income> incomesList = session.
                    createCriteria(Income.class).list();
            for (Income income : incomesList) {
                session.delete(income);
            }
            
            List<Status> statusList = session.
                createCriteria(Status.class).list();
            
            for (Status status : statusList) {
                session.delete(status);
            }
            
            session.getTransaction().commit();
            
            this.refresh();
        }
    }

    @FXML
    private void newExpenseAction(ActionEvent event) throws IOException {
        //Fill stage with content
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLExpense.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Dodaj koszt");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void newIncomeAction(ActionEvent event) throws IOException {
        //Fill stage with content
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLIncome.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Dodaj dochód");
        stage.setScene(new Scene(root));
        stage.show();
    }
    
    @FXML
    private void statsAction(ActionEvent event) throws IOException {
        //Fill stage with content
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLStats.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Statystyki");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        renderTableIncome();
        renderTableExpense();

        String parameter = String.format("%.2f", this.accountBalance);
        initStatus(parameter);
    }

    private void initStatus(String accountBalance) {
        Session session = db.getSessionFactory().openSession();
        List<Status> statusList = session.
                createCriteria(Status.class).list();

        Status status;
        if (statusList.isEmpty()) {
            status = new Status();
            status.setSettlementPeriod(new Date());
            session.beginTransaction();
            session.save(status);
            session.getTransaction().commit();

        } else {
            status = statusList.get(0);
        }

        Format df = new SimpleDateFormat("dd-MM-yyyy");
        settlementPeriod.setText(df.format(status.getSettlementPeriod()));

        balance.setText(accountBalance + " zł");
    }

    public void renderTableIncome() {
        this.accountBalance = 0.0;
        getIncome();
        columnTitleIncome.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        columnDescriptionIncome.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        columnAmountIncome.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        tableIncome.setItems(incomes);
    }

    public void renderTableExpense() {
        getExpense();
        columnTitleExpense.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        columnDescriptionExpense.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        columnAmountExpense.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        tableExpense.setItems(expenses);
    }

    public ObservableList<Income> getIncome() {
        ObservableList<Income> incomesList = FXCollections.observableArrayList();
        Session session = db.getSessionFactory().openSession();
        List<Income> incomeList = session.
                createCriteria(Income.class).list();
        for (Income income : incomeList) {
            incomes.add(new SimpleIncome(income.getTitle(), income.getDescription(), income.getAmount()));
            this.accountBalance += income.getAmount();
        }
        return incomesList;
    }

    public ObservableList<Expense> getExpense() {
        ObservableList<Expense> incomesList = FXCollections.observableArrayList();
        Session session = db.getSessionFactory().openSession();
        List<Expense> expenseList = session.
                createCriteria(Expense.class).list();
        for (Expense expense : expenseList) {
            expenses.add(new SimpleIncome(expense.getTitle(), expense.getDescription(), expense.getAmount()));
            this.accountBalance -= expense.getAmount();
        }
        return incomesList;
    }

    @FXML
    private void handleRefreshAction(ActionEvent event) throws IOException {
        this.refresh();
    }
    
    private void refresh() {
        tableIncome.getItems().clear();
        tableExpense.getItems().clear();
        renderTableIncome();
        renderTableExpense();

        String parameter = String.format("%.2f", this.accountBalance);
        initStatus(parameter);
    }

}
