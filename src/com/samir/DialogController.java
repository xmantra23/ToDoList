package com.samir;

import com.samir.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {
    @FXML
    private TextField title;
    @FXML
    private TextArea details;
    @FXML
    private DatePicker datePicker;
    
    public TodoItem getDialogInput(){
        String title = this.title.getText().trim();
        String description = this.details.getText().trim();
        LocalDate deadline = this.datePicker.getValue();
        
        TodoItem newItem = new TodoItem(title,description,deadline);
        //TodoData.getInstance().addTodoItem(newItem);
        return newItem;
    }
    
    public void setFields(TodoItem item){
        title.setText(item.getShortDescription());
        details.setText(item.getDetails());
        datePicker.setValue(item.getDeadline());
    }
}
