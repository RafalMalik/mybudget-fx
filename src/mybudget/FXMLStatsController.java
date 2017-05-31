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
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * FXML Controller class
 *
 * @author Malina
 */
public class FXMLStatsController implements Initializable {

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private PieChart pieChart;

    @FXML
    private LineChart<Integer, Integer> lineChart;

    @FXML
    private AreaChart<String, Integer> areaChart;

    private HibernateUtil db;

    private List<Income> incomes;

    private List<Expense> expenses;

    Map<String, Double> stats = new HashMap<String, Double>();
    Map<String, Double> daysIncome = new HashMap<String, Double>();
    Map<String, Double> daysExpense = new HashMap<String, Double>();

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        incomes = getIncome();
        expenses = getExpense();
        initStats();
        initBarChart();
        initPieChart();
        initLineChart();
        initAreaChart();
    }

    private void initBarChart() {

        XYChart.Series series1 = new XYChart.Series();
        series1.getData().add(new XYChart.Data("Dochód", stats.get("incomeAmount")));
        series1.getData().add(new XYChart.Data("Koszt", stats.get("expenseAmount")));

        barChart.getData().addAll(series1);
    }

    private void initPieChart() {

        ObservableList<PieChart.Data> pieChartData
                = FXCollections.observableArrayList(
                        new PieChart.Data("Dochód", stats.get("incomeAmount")),
                        new PieChart.Data("Koszt", stats.get("expenseAmount")));
        pieChart.setData(pieChartData);
    }

    private void initLineChart() {
        getHistogram();
        Double value = 0.0;
        XYChart.Series seriesIncome = new XYChart.Series();
        seriesIncome.setName("Dochody");

        XYChart.Series seriesExpense = new XYChart.Series();
        seriesExpense.setName("Koszty");

        LocalDate start = LocalDate.parse(getSettlementPeriod()),
                end = LocalDate.now();

        LocalDate next = start.minusDays(1);
        while ((next = next.plusDays(1)).isBefore(end.plusDays(1))) {
            if (daysIncome.get(next.toString()) == null) {
                value = 0.0;
            } else {
                value = daysIncome.get(next.toString());
            }
            seriesIncome.getData().add(new XYChart.Data(next.toString(), value));

            if (daysExpense.get(next.toString()) == null) {
                value = 0.0;
            } else {
                value = daysExpense.get(next.toString());
            }
            seriesExpense.getData().add(new XYChart.Data(next.toString(), value));
        }

        lineChart.getData().add(seriesIncome);
        lineChart.getData().add(seriesExpense);
    }

    private void getHistogram() {
        getIncomeHistogram();
        getExpenseHistogram();
    }

    private void initStats() {
        double expenseAmount = 0, incomeAmount = 0;
        for (Expense expense : expenses) {
            expenseAmount += expense.getAmount();
        }
        for (Income income : incomes) {
            incomeAmount += income.getAmount();
        }

        stats.put("expenseAmount", expenseAmount);
        stats.put("incomeAmount", incomeAmount);
    }

    public List<Income> getIncome() {
        Session session = db.getSessionFactory().openSession();
        List<Income> incomeList = session.
                createCriteria(Income.class).list();
        return incomeList;
    }

    public List<Expense> getExpense() {
        Session session = db.getSessionFactory().openSession();
        List<Expense> expenseList = session.
                createCriteria(Expense.class).list();

        return expenseList;
    }

    public void getIncomeHistogram() {
        int i = 0;
        String value = "";
        String time = "";
        String sql = "SELECT sum(amount) as amount, DATE(action_date) as day FROM `income` GROUP BY CAST(action_date AS DATE)";
        SQLQuery query = db.getSessionFactory().openSession().createSQLQuery(sql);
        List<Object[]> history = query.list();
        for (Object[] row : history) {
            for (Object rowCol : row) {
                if (i == 0) {
                    value = rowCol.toString();
                } else {
                    time = rowCol.toString();
                }
                i++;
            }
            daysIncome.put(time, Double.parseDouble(value));
            i = 0;
        }
    }

    public void getExpenseHistogram() {
        int i = 0;
        String value = "";
        String time = "";
        String sql = "SELECT sum(amount) as amount, DATE(action_date) as day FROM `expense` GROUP BY CAST(action_date AS DATE)";
        SQLQuery query = db.getSessionFactory().openSession().createSQLQuery(sql);
        List<Object[]> history = query.list();
        for (Object[] row : history) {
            for (Object rowCol : row) {
                if (i == 0) {
                    value = rowCol.toString();
                } else {
                    time = rowCol.toString();
                }
                i++;
            }
            daysExpense.put(time, Double.parseDouble(value));
            i = 0;
        }
    }

    public String getSettlementPeriod() {
        Session session = db.getSessionFactory().openSession();
        List<Status> statusList = session.
                createCriteria(Status.class).list();
        Status status = statusList.get(0);
        Format df = new SimpleDateFormat("YYYY-MM-dd");
        return df.format(status.getSettlementPeriod());
    }

    public void initAreaChart() {
        LocalDate start = LocalDate.parse(getSettlementPeriod()),
                end = LocalDate.now();
        Double value = 0.0;

        areaChart.setTitle("Dochody i koszty");

        XYChart.Series seriesIncome = new XYChart.Series();
        seriesIncome.setName("Dochod");
        seriesIncome.getData().add(new XYChart.Data("1", 4));

        XYChart.Series seriesExpense = new XYChart.Series();
        seriesExpense.setName("Koszt");
        seriesExpense.getData().add(new XYChart.Data("1", 20));

        LocalDate next = start.minusDays(1);
        while ((next = next.plusDays(1)).isBefore(end.plusDays(1))) {
            if (daysIncome.get(next.toString()) == null) {
                value = 0.0;
            } else {
                value = daysIncome.get(next.toString());
            }
            seriesIncome.getData().add(new XYChart.Data(next.toString(), value));

            if (daysExpense.get(next.toString()) == null) {
                value = 0.0;
            } else {
                value = daysExpense.get(next.toString());
            }
            seriesExpense.getData().add(new XYChart.Data(next.toString(), value));
        }
        
        areaChart.getData().addAll(seriesIncome, seriesExpense);
    }

}
