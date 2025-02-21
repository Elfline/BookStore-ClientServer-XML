
package client.owner.controller;

import client.owner.model.BookOwnerSalesModel;
import client.owner.view.BookOwnerSalesView;
import utilities.Transaction;
import utilities.XMLUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class BookOwnerSalesController {
    private BookOwnerSalesModel model;
    private BookOwnerSalesView view;
    private BookOwnerController mainController;


    public BookOwnerSalesController(BookOwnerSalesView view, BookOwnerSalesModel model, BookOwnerController mainController) {
        this.model = model;
        this.view = view;
        this.mainController = mainController;

        // Attach action listeners
        this.view.getRefreshButton().addActionListener(new RefreshButtonListener());

        // initialize socket connection and streams
        refresh();
    }

    public void refresh() {
        model.fetchTransactions();
        List<Transaction> transactions = model.getTransactions();
        Map<String, Double> revenueByMonth = model.getRevenueByMonth();
        view.showSalesData(transactions, revenueByMonth);
    }

    private class RefreshButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            refresh();
        }
    }
}
