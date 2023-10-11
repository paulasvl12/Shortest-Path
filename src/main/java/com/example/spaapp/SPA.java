package com.example.spaapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class HelloApplication extends Application {
    Label errorLabel = new Label();
    int pointAandBCount = 0;
    int[] start = {0, 0};
    int[] end = {0, 0};
    int clickCount = 0;
    Button getShortestPath = new Button("Shortest Path");
    Rectangle[][] gridCells;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        // Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        VBox vBox = new VBox(10);
        final GridPane[] gridPane = {initializeGrid(3, 3)};

        TextField customRows = new TextField("Enter rows");
        TextField customCols = new TextField("Enter cols");
        Button expand = new Button("Expand Grid");
        int[] gridSize = {3, 3};

        // used for coloring the shortest path
        gridCells = new Rectangle[3][3];

        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(gridPane[0], expand, getShortestPath, customRows, customCols, errorLabel);
        gridPane[0].setAlignment(Pos.CENTER);

        expand.setOnAction(e -> {
            try {
                int newNumRows = Integer.parseInt(customRows.getText());
                int newNumCols = Integer.parseInt(customCols.getText());

                gridSize[0] = newNumRows;
                gridSize[1] = newNumCols;
                if (gridCells.length != newNumRows || gridCells[0].length != newNumCols) {
                    gridCells = new Rectangle[newNumRows][newNumCols];
                }

                vBox.getChildren().remove(gridPane[0]);
                gridPane[0] = initializeGrid(gridSize[0], gridSize[1]);
                vBox.getChildren().add(0, gridPane[0]);
                pointAandBCount = 0;
            }
            catch(NumberFormatException exception){
                errorLabel.setText("Invalid number(s). Enter a valid number of rows/cols");
            }
        });

        Scene scene = new Scene(vBox, 200, 200);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private GridPane initializeGrid(int numRows, int numCols) {
        GridPane gridPane = new GridPane();
        Rectangle[][] cells = new Rectangle[numRows][numCols];
        int[][] cellsGraph = new int[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                Rectangle cell = new Rectangle(50, 50); // Adjust the size as needed
                cell.setFill(Color.GRAY); // Set the initial color to white
                cell.setStroke(Color.BLACK);
                cell.setStrokeType(StrokeType.OUTSIDE);
                gridPane.add(cell, col, row);

                // Store the rectangle in the 2D array
                cells[row][col] = cell;
                cellsGraph[row][col] = 1;
                int finalRow = row;
                int finalCol = col;
                cell.setOnMouseClicked(e -> {
                            changeCellColor(cell, finalRow, finalCol);
                        }
                        );
            }
        }

        getShortestPath.setOnAction( e-> {
            List<int[]> shortestPath = shortestPath(cellsGraph, start, end);
            visualizeShortestPath(shortestPath, numRows, numCols, gridPane);
        });
        GridPane.setHalignment(gridPane, javafx.geometry.HPos.CENTER);
        GridPane.setValignment(gridPane, javafx.geometry.VPos.CENTER);
        return gridPane;
    }

    private void changeCellColor(Rectangle cell, int x, int y){
        if(pointAandBCount < 2){
            if (cell.getFill() == Color.GRAY) {
                cell.setFill(Color.RED);
                pointAandBCount++;
                if(clickCount == 0){
                    start[0] = x;
                    start[1] = y;
                    clickCount++;
                }
                if(clickCount == 1){
                    end[0] = x;
                    end[1] = y;
                }
            } else {
                cell.setFill(Color.GRAY);
            }
        } else if(pointAandBCount == 2){
            errorLabel.setText("Only pick two points: start and end.");
        }

    }

    private List<int[]> shortestPath(int[][] cells, int[] start, int[] end) {
        int rows = cells.length;
        int cols = cells[0].length;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // Create a 2D array to store the minimum distance to reach each cell
        int[][] distance = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(distance[i], Integer.MAX_VALUE);
        }
        distance[start[0]][start[1]] = 0;

        // Create a priority queue to select cells with the minimum distance
        PriorityQueue<int[]> queue = new PriorityQueue<>((a, b) -> distance[a[0]][a[1]] - distance[b[0]][b[1]]);
        queue.add(start);

        // Create a 2D array to store the parent of each cell
        int[][][] parent = new int[rows][cols][2]; // [row][col][0] stores parentRow, [row][col][1] stores parentCol
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }

        while (!queue.isEmpty()) {
            int[] current = queue.poll();

            if (current[0] == end[0] && current[1] == end[1]) {
                // Reconstruct the shortest path from end to start
                List<int[]> shortestPathCells = new ArrayList<>();
                int[] node = end;
                while (!Arrays.equals(node, start)) {
                    shortestPathCells.add(node);
                    int[] parentNode = parent[node[0]][node[1]];
                    node = parentNode;
                }
                shortestPathCells.add(start);
                Collections.reverse(shortestPathCells);
                return shortestPathCells;
            }

            for (int[] direction : directions) {
                int newRow = current[0] + direction[0];
                int newCol = current[1] + direction[1];
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols &&
                        cells[newRow][newCol] == 1) {
                    int newDistance = distance[current[0]][current[1]] + 1;
                    if (newDistance < distance[newRow][newCol]) {
                        distance[newRow][newCol] = newDistance;
                        parent[newRow][newCol][0] = current[0];
                        parent[newRow][newCol][1] = current[1];
                        queue.offer(new int[]{newRow, newCol});
                    }
                }
            }
        }

        return null; // No path found
    }




    private void visualizeShortestPath(List<int[]> path, int numRows, int numCols, GridPane gridPane) {
        if (path == null) {
            return;
        }

        for (int i=1; i < path.size()-1; i++) {
            int row = path.get(i)[0];
            int col = path.get(i)[1];
            Rectangle rectangle = (Rectangle) gridPane.getChildren().get(row * numCols + col);
            rectangle.setFill(Color.BLUE);
        }
    }
}

