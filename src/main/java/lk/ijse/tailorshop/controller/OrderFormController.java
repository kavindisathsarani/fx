package lk.ijse.tailorshop.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.tailorshop.model.*;
import lk.ijse.tailorshop.model.Tm.GarmentCartTm;
import lk.ijse.tailorshop.model.Tm.MaterialCartTm;
import lk.ijse.tailorshop.repository.*;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class OrderFormController {
    @FXML
    private JFXButton btnAddToCart;

    @FXML
    private JFXComboBox<String> cmbGarmentId;

    @FXML
    private TableColumn<?, ?> colAction;

    @FXML
    private TableColumn<?, ?> colAmount;

    @FXML
    private TableColumn<?, ?> colCategory;

    @FXML
    private TableColumn<?, ?> colDescription;

    @FXML
    private TableColumn<?, ?> colGarmentId;

    @FXML
    private TableColumn<?, ?> colMaterialCost;

    @FXML
    private TableColumn<?, ?> colName;

    @FXML
    private TableColumn<?, ?> colQtyOnHand;

    @FXML
    private TableColumn<?, ?> colSize;

    @FXML
    private TableColumn<?, ?> colTotalPrice;

    @FXML
    private TableColumn<?, ?> colTowage;

    @FXML
    private Label lblCategory;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblMaterialCost;

    @FXML
    private Label lblName;

    @FXML
    private Label lblNetTotal;

    @FXML
    private Label lblOrderId;

    @FXML
    private Label lblQtyOnHand;

    @FXML
    private Label lblSize;

    @FXML
    private Label lblTotalPrice;

    @FXML
    private Label lblTowage;

    @FXML
    private AnchorPane pane;

    @FXML
    private TableView<GarmentCartTm> tblGarmentCart;

    @FXML
    private TextField txtCustomerId;

    @FXML
    private TextField txtDuedate;

    @FXML
    private TextField txtQty;

    @FXML
    private TextField txtStatus;

    private ObservableList<GarmentCartTm> gcList = FXCollections.observableArrayList();
    private double netTotal = 0;

    public void initialize(){
        setCellValueFactory();
        loadNextOrderId();
        setDate();
        getGarmentId();
    }

    private void setCellValueFactory() {
        colGarmentId.setCellValueFactory(new PropertyValueFactory<>("garmentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colQtyOnHand.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colMaterialCost.setCellValueFactory(new PropertyValueFactory<>("materialCost"));
        colTowage.setCellValueFactory(new PropertyValueFactory<>("towage"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btnRemove"));
    }

    private void getGarmentId() {
        ObservableList<String> obList = FXCollections.observableArrayList();
        try {
            List<String> codeList = GarmentRepo.getIds();
            for (String code : codeList) {
                obList.add(code);
            }

            cmbGarmentId.setItems(obList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setDate() {
        LocalDate now = LocalDate.now();
        lblDate.setText(String.valueOf(now));
    }

    private void loadNextOrderId() {
        try {
            String currentId = OrderRepo.currentId();
            String nextId = nextId(currentId);

            lblOrderId.setText(nextId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String nextId(String currentId) {
        if (currentId != null) {
            String[] split = currentId.split("O");
            // System.out.println("Arrays.toString(split) = " + Arrays.toString(split));
            int id = Integer.parseInt(split[1]);    //2
            return "O" + ++id;

        }
        return "O1";
    }

    @FXML
    void btnAddToCartOnAction(ActionEvent event) {
        String garmentId = cmbGarmentId.getValue();
        String name = lblName.getText();
        String description = lblDescription.getText();
        String category = lblCategory.getText();
        String size = lblSize.getText();
        int qty = Integer.parseInt(txtQty.getText());
        double materialCost = Double.parseDouble(lblMaterialCost.getText());
        double towage = Double.parseDouble(lblTowage.getText());
        double totalPrice = Double.parseDouble(lblTotalPrice.getText());
        double amount = qty * totalPrice;
        JFXButton btnRemove = new JFXButton("remove");
        btnRemove.setCursor(Cursor.HAND);

        btnRemove.setOnAction((e) -> {
            ButtonType yes = new ButtonType("yes", ButtonBar.ButtonData.OK_DONE);
            ButtonType no = new ButtonType("no", ButtonBar.ButtonData.CANCEL_CLOSE);

            Optional<ButtonType> type = new Alert(Alert.AlertType.INFORMATION, "Are you sure to remove?", yes, no).showAndWait();

            if(type.orElse(no) == yes) {
                int selectedIndex = tblGarmentCart.getSelectionModel().getSelectedIndex();
                gcList.remove(selectedIndex);

                tblGarmentCart.refresh();
                calculateNetTotal();
            }
        });

        for (int i = 0; i < tblGarmentCart.getItems().size(); i++) {
            if (garmentId.equals(colGarmentId.getCellData(i))) {
                qty += gcList.get(i).getQty();
                amount = totalPrice * qty;

                gcList.get(i).setQty(qty);
                gcList.get(i).setAmount(amount);

                tblGarmentCart.refresh();
                calculateNetTotal();
                txtQty.setText("");
                return;
            }
        }

        GarmentCartTm garmentCartTm = new GarmentCartTm(garmentId, name, description, category,size,qty,materialCost,towage,totalPrice,amount, btnRemove);

        gcList.add(garmentCartTm);

        tblGarmentCart.setItems(gcList);
        txtQty.setText("");
        calculateNetTotal();

    }

    private void calculateNetTotal() {
        netTotal = 0;
        for (int i = 0; i < tblGarmentCart.getItems().size(); i++) {
            netTotal += (double) colAmount.getCellData(i);
        }
        lblNetTotal.setText(String.valueOf(netTotal));
    }

    @FXML
    void btnBackOnAction(ActionEvent event) throws IOException {
        AnchorPane anchorPane = FXMLLoader.load(getClass().getResource("/view/dashboard_form.fxml"));
        Stage stage = (Stage) pane.getScene().getWindow();

        stage.setScene(new Scene(anchorPane));
        stage.setTitle("Dashboard Form");
        stage.centerOnScreen();

    }

    @FXML
    void btnPlaceOrderOnAction(ActionEvent event) {
        String orderId = lblOrderId.getText();
        Date startDate = Date.valueOf(LocalDate.now());
        Date dueDate= Date.valueOf(txtDuedate.getText());
        String status = txtStatus.getText();
        String customerId=txtCustomerId.getText();

        var order = new Order(orderId, startDate, dueDate,status,customerId);

        List<OrderDetail> odList = new ArrayList<>();
        for (int i = 0; i < tblGarmentCart.getItems().size(); i++) {
            GarmentCartTm tm = gcList.get(i);

            OrderDetail gd = new OrderDetail(
                    orderId,
                    tm.getGarmentId(),
                    tm.getQty()
            );
            odList.add(gd);
        }

        PlaceOrder po = new PlaceOrder(order, odList);

        try {
            boolean isPlaced = PlaceOrderRepo.placeOrder(po);
            if(isPlaced) {
                new Alert(Alert.AlertType.CONFIRMATION, "order placed!").show();
            } else {
                new Alert(Alert.AlertType.WARNING, "order not placed!").show();
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }

    }

    @FXML
    void cmbGarmentIdOnAction(ActionEvent event) {
        String garmentId = cmbGarmentId.getValue();
        try {
            Garment garment = GarmentRepo.searchByCode(garmentId);
            if (garment != null) {
                lblName.setText(garment.getName());
                lblDescription.setText(garment.getDescription());
                lblCategory.setText(garment.getCategory());
                lblSize.setText(garment.getSize());
                lblQtyOnHand.setText(String.valueOf(garment.getQtyOnHand()));
                lblMaterialCost.setText(String.valueOf(garment.getMaterialCost()));
                lblTowage.setText(String.valueOf(garment.getTowage()));
                lblTotalPrice.setText(String.valueOf(garment.getTotalPrice()));

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        txtQty.requestFocus();
    }

    @FXML
    void txtQtyOnAction(ActionEvent event) {
        btnPlaceOrderOnAction(event);

    }
}
