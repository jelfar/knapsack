import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.PriorityQueue;
public class Knapsack {
    private static ArrayList<KnapsackItem> itemList; //list of items
    private static ArrayList<KnapsackItem> knapsack; //actual knapsack
    private static int maxCapacity; //max capacity of the knapsack
    private int curCapacity; //running total for capacity of knapsack
    private int curValue; //running total for value of knapsack
    private int numItems;

    //for brute force
    private boolean[] solution;
    private boolean[] current;
    private int curBestValue;
    private int curBestWeight;

    //for B&B
    private static ArrayList<KnapsackItem> orderedItemList; //list of items

    void readfile_graph(String filename) throws FileNotFoundException {
        FileInputStream in = new FileInputStream(new File(filename));
        Scanner sc = new Scanner(in);
        itemList = new ArrayList<KnapsackItem>();
        knapsack = new ArrayList<KnapsackItem>();

        numItems = sc.nextInt();

        solution = new boolean[numItems];
        for(int i = 0; i < numItems; i++) {
            itemList.add(new KnapsackItem(sc.nextInt(),
                                          sc.nextInt(),
                                          sc.nextInt()));
        }
        maxCapacity = sc.nextInt();
    }

    public long runKnapsack(String option) {
        long start = System.nanoTime();
        if(option.equals("Brute Force")) {
            //run brute force
            current = new boolean[numItems];
            bruteForce(numItems-1);

            //output
            System.out.println("Using Brute force the best feasible" +
                                " solution found: " + this.curBestValue + 
                                " " + this.curBestWeight);
            printBruteForceKnapsack();
        } else if(option.equals("Greedy")) {
            greedy();
            System.out.println("Greedy solution (not necessarily optimal): " + 
                                this.curValue + " " + this.curCapacity);
            printBruteForceKnapsack();
        } else if(option.equals("Dynamic")) {
            dynamic();
            System.out.println("Dynamic Programming solution: " +
                               this.curValue + " " + this.curCapacity);
            printBruteForceKnapsack();
        } else if(option.equals("Branch-And-Bound")) {

            branchAndBound();

            System.out.println("Using Branch and Bound the best feasible solution found: " +
                                this.curValue + " " + this.curCapacity);
            printBruteForceKnapsack();
        }
        long end = System.nanoTime();
        
        return end - start;
    }

    private void printBruteForceKnapsack() {
        for(int i = 0; i < numItems; i++) {
            if(solution[i]) {
                System.out.print(itemList.get(i).index + " ");
            }
        }
        System.out.println();
    }

    private void printKnapsack() {
        for(int i = 0; i < knapsack.size(); i++) {
            System.out.print(knapsack.get(i).index + " ");
        }
        System.out.println();
    }

    /**
      * Greedy algorithm to solve knapsack.
      * Sorts in ascending order by value/weight ratios.
      */
    private void greedy() {
        ArrayList<KnapsackItem> orderedList = new ArrayList<KnapsackItem>();
        KnapsackItem curItem; //item we are on
        double curRatio; //ratio of current list item
        double orderedCurRatio; //ratio of each ordered list item
        boolean added;

        //Sort the items in ascending order from
        //smallest to highest based on (value / weight)
        orderedList.add(itemList.get(0));
        for(int i = 1; i < itemList.size(); i++) {
            added = false;
            curItem = itemList.get(i);
            curRatio = (double)curItem.value / curItem.weight;

            for(int j = 0; j < orderedList.size(); j++) {
                KnapsackItem orderedItem = orderedList.get(j);
                orderedCurRatio = (double)orderedItem.value / orderedItem.weight;

                if(curRatio < orderedCurRatio) {
                    orderedList.add(j, curItem);
                    added = true;
                    break;
                }
            }

            if(!added) {
                orderedList.add(curItem);
            }
        }

        //now pick each item and fill knapsack
        //as long as it doesn't exceed capacity
        while(curCapacity < maxCapacity && !orderedList.isEmpty()) {
            int highestValueIndex = orderedList.size() - 1;
            curItem = orderedList.remove(highestValueIndex);

            if(curItem.weight + curCapacity <= maxCapacity) {
                //knapsack.add(curItem);
                solution[curItem.index - 1] = true;
                curValue += curItem.value;
                curCapacity += curItem.weight;
            }
        }
    }

    private void bruteForce(int numSize) {
        if(numSize < 0) {
            curCapacity = 0;
            curValue = 0;
            for(int i = 0; i < numItems; i++) {
                if(current[i]) {
                    curCapacity += itemList.get(i).weight;
                    curValue += itemList.get(i).value;
                }
            }

            if(curCapacity <= maxCapacity && curValue > curBestValue) {
                curBestValue = curValue;
                curBestWeight = curCapacity;
                for(int j = 0; j < solution.length; j++) {
                    solution[j] = current[j];
                }
            }
        } else {
            current[numSize] = true;
            bruteForce(numSize - 1);
            current[numSize] = false;
            bruteForce(numSize - 1);
        }
    }

    /**
      * Uses dynamic programming to solve knapsack.
      */
    public void dynamic() {
        int width = maxCapacity + 1;
        int height = itemList.size() + 1;
        int offset;
        int[][] matrix = new int[width][height];
        this.curCapacity = 0;
        this.curValue = 0;

        //init first column with zeroes
        for(int i = 0; i < width; i++) {
            matrix[i][0] = 0;
        }

        //init first row with zeroes
        for(int i = 0; i < height; i++) {
            matrix[0][i] = 0;
        }

        //fill rest of table
        for(int i = 1; i < height; i++) {
            this.curCapacity = itemList.get(i - 1).weight;
            this.curValue = itemList.get(i-1).value;
            
            //matrix[i][j] = max(matrix[i][j-1], 
            //                   matrix[remainingCapacity][i-1] + value)
            for(int matrixCapacity = 1; matrixCapacity < width; matrixCapacity++) {
                matrix[matrixCapacity][i] = matrix[matrixCapacity][i - 1];
                if(this.curCapacity <= matrixCapacity) {
                    offset = matrixCapacity - this.curCapacity;
                    matrix[matrixCapacity][i] = Math.max(matrix[matrixCapacity][i],
                                               matrix[offset][i - 1] + this.curValue);
                }
            }
        }

        //reset variables to get total capacity
        //and value of knapsack
        this.curCapacity = 0;
        this.curValue = 0;

        //fill knapsack with appropriate items
        //through backtracing
        int backTraceX = width - 1;
        int backTraceY = height - 1;
        while(backTraceY > 0 && backTraceX > 0) {
            if(matrix[backTraceX][backTraceY] != matrix[backTraceX][backTraceY - 1]) {
                KnapsackItem item = itemList.get(backTraceY - 1);
                solution[backTraceY - 1] = true;

                //decrement the matrix counter by item weight
                backTraceX -= item.weight;

                //update capacity and value vars
                this.curCapacity += item.weight;
                this.curValue += item.value;
            }
            backTraceY--;
        }
    }

    /**
      * Branch And Bound to solve knapsack.
      */
    public void branchAndBound() {
        ArrayList<KnapsackItem> orderedList = new ArrayList<KnapsackItem>();
        KnapsackItem curItem; //item we are on
        double curRatio; //ratio of current list item
        double orderedCurRatio; //ratio of each ordered list item
        boolean added;

        //Sort the items in descending order from
        //highest to smallest based on (value / weight)
        orderedList.add(itemList.get(0));
        for(int i = 1; i < itemList.size(); i++) {
            added = false;
            curItem = itemList.get(i);
            curRatio = (double)curItem.value / curItem.weight;

            for(int j = 0; j < orderedList.size(); j++) {
                KnapsackItem orderedItem = orderedList.get(j);
                orderedCurRatio = (double)orderedItem.value / orderedItem.weight;

                //order items greatest to smallest
                if(curRatio >= orderedCurRatio) {
                    orderedList.add(j, curItem);
                    added = true;
                    break;
                }
            }

            if(!added) {
                orderedList.add(curItem);
            }
        }

        //set global ordered list for getBound() in node class
        this.orderedItemList = orderedList;
        
        //priority queue holding bound values
        PriorityQueue<Node> queue = new PriorityQueue<Node>();

        //current best node
        Node best = new Node();

        //root node
        Node root = new Node();
        
        //calculate upper bound of root and add it to queue
        root.getBound();
        queue.add(root);

        while(!queue.isEmpty()) {
            //remove next node
            Node curNode = queue.remove();

            //the current node's upper bound must be 
            //greater than the current value
            if(curNode.bound > best.value && curNode.index < orderedList.size() - 1) {
                //Create "take" item
                Node left = new Node(curNode);
                KnapsackItem item = orderedList.get(curNode.index);

                //add curnode item weight to the new node
                left.weight += item.weight;

                //if we didn't go over capacity
                if(left.weight <= maxCapacity) {
                    left.value += item.value;
                    left.getBound();

                    //take the item if the new value is
                    //greater than the cur value
                    if(left.value > best.value) {
                        best = left;
                        this.curValue = left.value;
                        this.curCapacity = left.weight;
                        solution[left.index - 1] = true;
                    }
                    //add to queue if not a dead end
                    if(left.bound > best.value) {
                        queue.add(left);
                    }
                }

                Node right = new Node(curNode);
                right.getBound();

                //add to queue if not a dead end
                if(right.bound > best.value) {
                    queue.add(right);
                }
            }
        }
        
        //set solution variables
        this.curCapacity = best.weight;
        this.curValue = best.value;
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("ERROR: Incorrect number of arguments.");
            System.out.println("example: java Knapsack test.txt");
            return; 
        }

        Knapsack knapsack = new Knapsack();

        try {
            knapsack.readfile_graph(args[0]);
            System.out.println("time: " + knapsack.runKnapsack("Brute Force"));
            System.out.println("time: " + knapsack.runKnapsack("Greedy"));
            System.out.println("time: " + knapsack.runKnapsack("Dynamic"));
            System.out.println("time: " + knapsack.runKnapsack("Branch-And-Bound"));
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: File not found.");
        }
    }
    class Node implements Comparable<Node> {
        public ArrayList<KnapsackItem> solutionItems;
        public int index;
        public int bound;
        public int value;
        public int weight;

        public Node() {
            solutionItems = new ArrayList<KnapsackItem>();
        }

        public Node(Node parent) {
            solutionItems = new ArrayList<KnapsackItem>();
            index = parent.index + 1;
            bound = parent.bound;
            value = parent.value;
            weight = parent.weight;
        }

        public int compareTo(Node other) {
            return other.bound - bound;
        }

        public void getBound() {
            int indexCpy = index;
            double weightCpy = weight;
            bound = value;
            KnapsackItem item;
            do {
                item = orderedItemList.get(indexCpy);
                if(weightCpy + item.weight > maxCapacity) {
                    break;
                } else {
                    weightCpy += item.weight;
                    bound += item.value;
                    indexCpy++;
                }
            } while(indexCpy < numItems);
           bound += (maxCapacity - weightCpy) * (item.value / item.weight);
        }
    }

    class KnapsackItem {
        public int index; //element position in list of items
        public int value;
        public int weight;

        public KnapsackItem(int i, int v, int w) {
            this.index = i;
            this.value = v;
            this.weight = w;
        }

        public String toString() {
            return "{KnapsackItem:" + this.index + " value:" 
                + this.value + " weight: " + this.weight + "}";
        }
    }
}
