package application;
	
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;



public class Main extends Application {

	private final TextField idField = new TextField();
	private final TextField nameField = new TextField();
	private final TextField ageField = new TextField();
	private final ComboBox<String> genderBox = new ComboBox<>();
	private final TextField phoneField = new TextField();
	private final TextField diagnosisField = new TextField();
	private final DatePicker admissionDatePicker = new DatePicker();
	  
		//===========================
		// TABLE & SEARCH CONTROLS
		//============================
	private final TextField searchField = new TextField();
	private final TableView<Patient> patientTable = new TableView<>();
	private final ObservableList<Patient> patientData = FXCollections.observableArrayList();
	    
	  // Status label
	private final Label statusLabel = new Label("Enter patient details.");
    
	private PatientHandler handler;
	  
	@Override
	public void start(Stage primaryStage) {
		handler = new PatientHandler(this);
		
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setTop(createHeader());
        root.setCenter(createCenterContent());
        root.setBottom(createFooter());
        
        handler.loadPatients();
        
        Scene scene = new Scene(root, 1060, 580);
        primaryStage.setTitle("Hospital Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
	}
			
			//==============================
			// TOP REGION: LAYOUT SECTIONS
			//==============================
        private VBox createHeader() {
            Label heading = new Label("Hospital Management System");
            heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            Label subtitle = new Label("Patient Registration & Clinical Records");

            VBox header = new VBox(5, heading, subtitle);
            header.setPadding(new Insets(0, 0, 20, 0));
            return header;
        }
        
        private HBox createCenterContent() {
            VBox leftPane = createFormPane();
            VBox rightPane = createRecordsPane();
            HBox.setHgrow(rightPane, Priority.ALWAYS);

            return new HBox(20, leftPane, rightPane);
        }
        
        private VBox createFormPane() {
            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(14);
            form.setPadding(new Insets(10, 15, 10, 0));

            configureFormControls();

            addFormRow(form, 0, "Patient ID:", idField);
            addFormRow(form, 1, "Full name:", nameField);
            addFormRow(form, 2, "Age:", ageField);
            addFormRow(form, 3, "Gender:", genderBox);
            addFormRow(form, 4, "Phone:", phoneField);
            addFormRow(form, 5, "Diagnosis:", diagnosisField);
            addFormRow(form, 6, "Admission date:", admissionDatePicker);

            ColumnConstraints labelCol = new ColumnConstraints(120);
            ColumnConstraints fieldCol = new ColumnConstraints();
            fieldCol.setHgrow(Priority.ALWAYS);
            form.getColumnConstraints().addAll(labelCol, fieldCol);

            HBox buttonRow = createFormButtons();

            VBox leftPane = new VBox(14, form, buttonRow);
            leftPane.setPrefWidth(390);
            return leftPane;
        }
        
        private VBox createRecordsPane() {
        	HBox searchBar = createSearchBar();
        	configureTable();
        	VBox.setVgrow(patientTable, Priority.ALWAYS);
        	
        	return new VBox(12, searchBar, patientTable);
        }
        
        private VBox createFooter() {
            VBox footer = new VBox(8, statusLabel, new Label("JavaFX • JDBC • MySQL"));
            footer.setPadding(new Insets(15, 0, 0, 0));
            return footer;
        }
	        
			//==============================
			// LEFT SIDE OF CENTER
			//==============================
        private void configureFormControls() {
            idField.setPromptText("Auto-generated");
            idField.setEditable(false);
            idField.setStyle("-fx-background-color: #efefef;");

            nameField.setPromptText("Full name");
            ageField.setPromptText("Age in years (0-125)");

            genderBox.getItems().addAll("Female", "Male", "Non-binary", "Prefer not to say");
            genderBox.setPromptText("Select gender");

            phoneField.setPromptText("e.g. 416-555-0199");
            diagnosisField.setPromptText("Reason for visit");
            admissionDatePicker.setPromptText("Select admission date");
        }
        
		
		//==============================
		//   CRUD BUTTONS
		//==============================
        private HBox createFormButtons() {
            Button addButton = new Button("Add Patient");
            addButton.setDefaultButton(true);
            addButton.setOnAction(handler);

            Button updateButton = new Button("Update");
            updateButton.setOnAction(handler);

            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(handler);

            Button clearButton = new Button("Clear");
            clearButton.setOnAction(handler);

            HBox buttonRow = new HBox(10, addButton, updateButton, deleteButton, clearButton);
            buttonRow.setAlignment(Pos.CENTER_RIGHT);
            return buttonRow;
        }
        
		//==============================
		//      SEARCH BUTTONS
		//==============================
        private HBox createSearchBar() {
            searchField.setPromptText("Search by name or phone...");
            HBox.setHgrow(searchField, Priority.ALWAYS);

            Button searchButton = new Button("Search");
            searchButton.setOnAction(handler);

            Button showAllButton = new Button("Show All");
            showAllButton.setOnAction(handler);

            HBox searchBar = new HBox(10, searchField, searchButton, showAllButton);
            searchBar.setAlignment(Pos.CENTER_LEFT);
            return searchBar;
        }
        
        @SuppressWarnings("unchecked")
        private void configureTable() {
            TableColumn<Patient, Integer> idCol = createColumn("ID", "patientId", 45);
            TableColumn<Patient, String> nameCol = createColumn("Full Name", "name", 130);
            TableColumn<Patient, Integer> ageCol = createColumn("Age", "age", 45);
            TableColumn<Patient, String> genderCol = createColumn("Gender", "gender", 95);
            TableColumn<Patient, String> phoneCol = createColumn("Phone", "phone", 105);
            TableColumn<Patient, String> diagnosisCol = createColumn("Diagnosis", "diagnosis", 140);
            TableColumn<Patient, LocalDate> dateCol = createColumn("Admission Date", "admissionDate", 105);

            patientTable.getColumns().addAll(
                    idCol, nameCol, ageCol, genderCol, phoneCol, diagnosisCol, dateCol
            );
            patientTable.setItems(patientData);

            patientTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, selectedPatient) -> {
                        if (selectedPatient != null) {
                            populateForm(selectedPatient);
                        }
                    }
            );
        }
        
		//==============================
		//     COLUMN FORMATER
		//==============================
        private <T> TableColumn<Patient, T> createColumn(String title, String property, double width) {
            TableColumn<Patient, T> column = new TableColumn<>(title);
            column.setCellValueFactory(new PropertyValueFactory<>(property));
            column.setPrefWidth(width);
            return column;
        }

        private void addFormRow(GridPane grid, int row, String labelText, Control control) {
            Label label = new Label(labelText);
            label.setStyle("-fx-font-weight: bold;");
            control.setMaxWidth(Double.MAX_VALUE);

            grid.add(label, 0, row);
            grid.add(control, 1, row);
        }
        
        public void populateForm(Patient patient) {
            idField.setText(String.valueOf(patient.getPatientId()));
            nameField.setText(patient.getName());
            ageField.setText(String.valueOf(patient.getAge()));
            genderBox.setValue(patient.getGender());
            phoneField.setText(patient.getPhone());
            diagnosisField.setText(patient.getDiagnosis());
            admissionDatePicker.setValue(patient.getAdmissionDate());
            setStatus("Selected patient ID " + patient.getPatientId() + ".", false);
        }
        
        public void clearForm() {
            clearFields();
            setStatus("Form cleared.", false);
        }
        
        public void clearFields() {
            idField.clear();
            nameField.clear();
            ageField.clear();
            genderBox.getSelectionModel().clearSelection();
            phoneField.clear();
            diagnosisField.clear();
            admissionDatePicker.setValue(null);
            patientTable.getSelectionModel().clearSelection();
        }
        
        public void setStatus(String message, boolean error) {
            statusLabel.setText(message);
            statusLabel.setStyle(error ? "-fx-text-fill: #b00020;" : "-fx-text-fill: #167545;");
        }
        
		//==============================
		//          GETTERS
		//==============================
        public TextField getIdField() { return idField; }
        public TextField getNameField() { return nameField; }
        public TextField getAgeField() { return ageField; }
        public ComboBox<String> getGenderBox() { return genderBox; }
        public TextField getPhoneField() { return phoneField; }
        public TextField getDiagnosisField() { return diagnosisField; }
        public DatePicker getAdmissionDatePicker() { return admissionDatePicker; }
        public TextField getSearchField() { return searchField; }
        public ObservableList<Patient> getPatientData() { return patientData; }



	public static void main(String[] args) {
		launch(args);
	}
}
