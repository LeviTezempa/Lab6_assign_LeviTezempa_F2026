package application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;


public class PatientHandler implements EventHandler<ActionEvent> {

    private static final String URL = "jdbc:mysql://localhost:3306/hospital_db";
    private static final String USER = "#";
    private static final String PASSWORD =  "#";
    
    private final Main app;
    
    public PatientHandler(Main app) {
        this.app = app;
    }
    
    
	@Override
	public void handle(@SuppressWarnings("exports") ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() instanceof Button clickedButton) {
			
			String action = clickedButton.getText();
			
			
            switch (action) {
            case "Add Patient" -> addPatient();
            case "Update" -> updatePatient();
            case "Delete" -> deletePatient();
            case "Clear" -> app.clearForm();
            case "Search" -> searchPatients();
            case "Show All" -> {
                app.getSearchField().clear();
                loadPatients();
            }
          }
        }
    }

	//=================================
	//          VALIDATION 
	//==================================
    private boolean validateForm() {
        String name = app.getNameField().getText().trim();
        String ageValue = app.getAgeField().getText().trim();
        String gender = app.getGenderBox().getValue();
        String phone = app.getPhoneField().getText().trim();
        String diagnosis = app.getDiagnosisField().getText().trim();
        LocalDate admissionDate = app.getAdmissionDatePicker().getValue();

        if (name.isEmpty() || ageValue.isEmpty() || gender == null
                || phone.isEmpty() || diagnosis.isEmpty() || admissionDate == null) {
            app.setStatus("Please complete all fields.", true);
            return false;
        }

        int age;
        try {
            age = Integer.parseInt(ageValue);
        } catch (NumberFormatException ex) {
            app.setStatus("Age must be a whole number.", true);
            return false;
        }

        if (age < 0 || age > 125) {
            app.setStatus("Age must be between 0 and 125.", true);
            return false;
        }

        if (!phone.matches("^[0-9+\\-()\\s]{7,20}$")) {
            app.setStatus("Enter a valid phone number.", true);
            return false;
        }

        if (admissionDate.isAfter(LocalDate.now())) {
            app.setStatus("Admission date cannot be in the future.", true);
            return false;
        }

        return true;
    }

	//=================================
	//         READ METHOD 
	//==================================
	void loadPatients() {
		// TODO Auto-generated method stub
        app.getPatientData().clear();
        String sql = "SELECT patient_id, name, age, gender, phone, diagnosis, admission_date "
                   + "FROM patients ORDER BY patient_id ASC";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                app.getPatientData().add(new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("diagnosis"),
                        rs.getDate("admission_date").toLocalDate()
                ));
            }
            app.setStatus("Loaded " + app.getPatientData().size() + " patient record(s).", false);
        } catch (SQLException ex) {
            app.setStatus("Database error: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }


	//=================================
	//       SEARCH METHOD
	//==================================
	private void searchPatients() {
		// TODO Auto-generated method stub
        String query = app.getSearchField().getText().trim();
        if (query.isEmpty()) {
            loadPatients();
            return;
        }

        app.getPatientData().clear();
        String sql = "SELECT patient_id, name, age, gender, phone, diagnosis, admission_date "
                   + "FROM patients WHERE name LIKE ? OR phone LIKE ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            String wildcard = "%" + query + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    app.getPatientData().add(new Patient(
                            rs.getInt("patient_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("phone"),
                            rs.getString("diagnosis"),
                            rs.getDate("admission_date").toLocalDate()
                    ));
                }
            }
            app.setStatus("Found " + app.getPatientData().size() + " matching record(s).", false);
        } catch (SQLException ex) {
            app.setStatus("Database error: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
	}


	//=================================
	//       DELETE METHOD
	//==================================
	private void deletePatient() {
		// TODO Auto-generated method stub
        if (app.getIdField().getText().isEmpty()) {
            app.setStatus("Select a patient from the table to delete.", true);
            return;
        }

        int patientId = Integer.parseInt(app.getIdField().getText().trim());
        String patientName = app.getNameField().getText().trim();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Patient Record");
        confirm.setContentText("Are you sure you want to delete " + patientName + " (ID: " + patientId + ")?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM patients WHERE patient_id = ?";

            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, patientId);

                int count = ps.executeUpdate();
                if (count == 1) {
                    loadPatients();
                    app.clearFields();
                    app.setStatus("Patient record deleted.", false);
                } else {
                    app.setStatus("No record was deleted.", true);
                }
            } catch (SQLException ex) {
                app.setStatus("Database error: " + ex.getMessage(), true);
                ex.printStackTrace();
            }
        }
    }

	//=================================
	//       UPDATE METHOD
	//==================================
	private void updatePatient() {
		// TODO Auto-generated method stub
        if (app.getIdField().getText().isEmpty()) {
            app.setStatus("Select a patient from the table to update.", true);
            return;
        }
        if (!validateForm()) return;

        int patientId = Integer.parseInt(app.getIdField().getText().trim());
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, "
                   + "phone = ?, diagnosis = ?, admission_date = ? WHERE patient_id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, app.getNameField().getText().trim());
            ps.setInt(2, Integer.parseInt(app.getAgeField().getText().trim()));
            ps.setString(3, app.getGenderBox().getValue());
            ps.setString(4, app.getPhoneField().getText().trim());
            ps.setString(5, app.getDiagnosisField().getText().trim());
            ps.setDate(6, Date.valueOf(app.getAdmissionDatePicker().getValue()));
            ps.setInt(7, patientId);

            int count = ps.executeUpdate();
            if (count == 1) {
                loadPatients();
                app.clearFields();
                app.setStatus("Patient record updated successfully!", false);
            } else {
                app.setStatus("No record was updated.", true);
            }
        } catch (SQLException ex) {
            app.setStatus("Database error: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }


	//=================================
	//       CREATE METHOD
	//==================================
	private void addPatient() {
		// TODO Auto-generated method stub
        if (!validateForm()) return;

        String sql = "INSERT INTO patients (name, age, gender, phone, diagnosis, admission_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, app.getNameField().getText().trim());
            ps.setInt(2, Integer.parseInt(app.getAgeField().getText().trim()));
            ps.setString(3, app.getGenderBox().getValue());
            ps.setString(4, app.getPhoneField().getText().trim());
            ps.setString(5, app.getDiagnosisField().getText().trim());
            ps.setDate(6, Date.valueOf(app.getAdmissionDatePicker().getValue()));

            int count = ps.executeUpdate();
            if (count == 1) {
                loadPatients();
                app.clearFields();
                app.setStatus("Patient registered successfully!", false);
            } else {
                app.setStatus("No record was inserted.", true);
            }
        } catch (SQLException ex) {
            app.setStatus("Database error: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
	}
	
}
