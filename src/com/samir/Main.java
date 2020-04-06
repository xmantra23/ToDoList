package com.samir;

import com.samir.datamodel.TodoData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainwindow.fxml"));
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        primaryStage.setTitle("Todo List Application");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }
    
    @Override
    public void init() throws IOException{
        try{
            TodoData.getInstance().loadTodoItems();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        
    }
    
    @Override
    public void stop() throws IOException{
        try{
            TodoData.getInstance().storeTodoItems();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
