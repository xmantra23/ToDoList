package com.samir;

import com.samir.datamodel.TodoData;
import com.samir.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    private List<TodoItem> todoItems;
    
    @FXML
    private ListView<TodoItem> todoListView;
    
    @FXML
    private TextArea itemDetails;
    
    @FXML
    private Label deadlineLabel;
    
    @FXML
    private BorderPane mainBorderPane;
    
    @FXML
    private ContextMenu listViewContextMenu;
    
    @FXML
    private ToggleGroup radioGroup;
    
    private FilteredList<TodoItem> filteredList;
    private Predicate<TodoItem> wantAllItems;
    private Predicate<TodoItem> wantTodaysItems;
    private Predicate<TodoItem> wantTomorrowsItems;
    private Predicate<TodoItem> wantPastDueItems;
    //methods
    
    public void initialize(){
        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observableValue, TodoItem todoItem, TodoItem t1) {
                if(t1 != null){
                    itemDetails.setText(t1.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadlineLabel.setText(df.format(t1.getDeadline()));
                }
            }
        });
    
        TodoComparator todoItemComparator = new TodoComparator(); //comparator for sorting
        wantAllItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return true;
            }
        };
        wantTodaysItems = todoItem -> (todoItem.getDeadline().equals(LocalDate.now())); //using lambdas. same as above code.
        wantPastDueItems = todoItem -> (todoItem.getDeadline().isBefore(LocalDate.now()));
        wantTomorrowsItems = todoItem -> (todoItem.getDeadline().equals(LocalDate.now().plusDays(1)));
        
        filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(),wantAllItems);
        SortedList<TodoItem> sortedList = new SortedList<>(filteredList,todoItemComparator);
        
        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();
        
        listViewContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });
        MenuItem editMenuItem = new MenuItem("Edit");
        editMenuItem.setOnAction(actionEvent -> {
            showEditItemDialog();
        });
        
        listViewContextMenu.getItems().setAll(deleteMenuItem);
        listViewContextMenu.getItems().add(editMenuItem);
        
        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<TodoItem>(){
                    @Override
                    protected void updateItem(TodoItem todoItem, boolean empty) {
                        super.updateItem(todoItem, empty);
                        if(empty){
                            setText(null);
                        }else{
                            setText(todoItem.getShortDescription());
                            if(todoItem.getDeadline().isBefore(LocalDate.now().plusDays(1))){
                                setTextFill(Color.RED);
                            }else if(todoItem.getDeadline().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.BLUE);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener((observableValue, wasEmpty, isEmpty) -> {
                    if(isEmpty){
                        cell.setContextMenu(null);
                    }else{
                        cell.setContextMenu(listViewContextMenu);
                    }
                });
                return cell;
            };
        });
    }
    
    private void deleteItem(TodoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure ? Press OK to delete");
        Optional<ButtonType> result = alert.showAndWait();
        
        if(result.isPresent() && (result.get() == ButtonType.OK)){
            TodoData.getInstance().deleteTodoItem(item);
        }
    }
    
    @FXML
    private void handleRadioButton(){
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        RadioButton selectedButton = (RadioButton) radioGroup.getSelectedToggle();
        String selectedButtonValue = selectedButton.getId();
        
        if(selectedButtonValue.equals("dueTodayButton")){
            filteredList.setPredicate(wantTodaysItems);
        }else if(selectedButtonValue.equals("pastDueButton")){
            filteredList.setPredicate(wantPastDueItems);
        }else if(selectedButtonValue.equals("dueTomorrowButton")) {
            filteredList.setPredicate(wantTomorrowsItems);
        }else {
            filteredList.setPredicate(wantAllItems);
        }
        selectItem(selectedItem);
    }
    private void selectItem(TodoItem selectedItem){
        if(!isEmpty()){
            if(filteredList.contains(selectedItem))
                todoListView.getSelectionModel().select(selectedItem);
            else
                todoListView.getSelectionModel().selectFirst();
        }
    }
    
    private boolean isEmpty(){
        if(filteredList.isEmpty()){
            itemDetails.clear();
            deadlineLabel.setText("");
            return true;
        }
        return false;
    }
    
    @FXML
    private void closeProgram(){
        try{
            TodoData.getInstance().storeTodoItems();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        Platform.exit();
    }
    
    @FXML
    private void handleKeyPressed(KeyEvent key){
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        if(item != null && key.getCode().equals(KeyCode.DELETE)){
            deleteItem(item);
        }
    }
    
    public void handleClickListView(){
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        StringBuilder sb = new StringBuilder(item.getDetails());
        sb.append("\n\n");
        sb.append("Due Date: " + item.getDeadline().toString());
        itemDetails.setText(sb.toString());
    }
    
    @FXML
    public void showNewItemDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add a new To-do Item");
        dialog.setHeaderText("Use this dialog to create a new todo item");
    
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch(IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            DialogController controller = fxmlLoader.getController();
            TodoItem newItem = controller.getDialogInput();
            TodoData.getInstance().addTodoItem(newItem);
            todoListView.getSelectionModel().select(newItem);
        }
    }
    
    @FXML
    public void showEditItemDialog(){
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem == null){
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Edit: " + selectedItem.getShortDescription());
        dialog.setHeaderText("Use this dialog to edit a todo item");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        
        DialogController controller = fxmlLoader.getController();
        controller.setFields(selectedItem);
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if(result.isPresent() && result.get() == ButtonType.OK){
            TodoItem newItem = controller.getDialogInput();
            TodoData.getInstance().replaceTodoItem(selectedItem,newItem);
        }
    }
    
    private class TodoComparator implements Comparator<TodoItem>{
        @Override
        public int compare(TodoItem o1, TodoItem o2) {
            return o1.getDeadline().compareTo(o2.getDeadline());
        }
    }
}
