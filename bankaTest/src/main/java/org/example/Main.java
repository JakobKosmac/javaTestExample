package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/* Napiši program, ki za poljubno izbrano časovno obdobje iz podatkovne baze, izpiše tečajnico za izbrane valute, poleg tabelaričnega izpisa naj se izriše še graf.
Dodatna funkcionalnost naj bo še izračun oportunitetnih zaslužkov/izgub med dvema poljubno izbranima valutama za poljubno izbrani termin iz podatkovne baze.

Kot vir vrednosti podatkovne baze naj se uporabi tečajna lista BSI (Prenos časovnih serij od 2007 http://www.bsi.si/_data/tecajnice/dtecbs-l.xml)

Program naj bo napisan v programskem jeziku Java, brez nepotrebnih odvisnosti, vsebovati mora prevajalno skripto, zaželjen je Maven, lahko tudi ANT. Program mora biti zapakiran v arhivsko datoteko, ki omogoča uporabniku prijazno izvajanje.*/

// Functions: readerFunction, tableFunction, graphFunction.
// Bonus Function: calculateFunction

// readerFunction - reads the XML from url
// tableFunction - displays table
// graphFunction - draws graph
// calculateFunction: uses two values and calculates gains/loses for a period


public class Main {

    static class ExchangeRate {
        String date;
        String currency;
        double rate;

        ExchangeRate(String date, String currency, double rate) {
            this.date = date;
            this.currency = currency;
            this.rate = rate;
        }
    }

    public static void main(String[] args) {

        String url = "https://www.bsi.si/_data/tecajnice/dtecbs-l.xml";

        try {
            String currencyInput = JOptionPane.showInputDialog(null,"Vpiši valute (npr. USD,JPY,INR):");
            if (currencyInput == null || currencyInput.isEmpty()) return;
            String[] currency = currencyInput.replace(" ", "").split(",");

            String dateZacetni = JOptionPane.showInputDialog(null,"Vpiši začetni datum (YYYY-MM-DD):");
            if (dateZacetni == null) return;

            String dateKoncni = JOptionPane.showInputDialog(null,"Vpiši končni datum (YYYY-MM-DD):");
            if (dateKoncni == null) return;

            List<ExchangeRate> rates =readerFunction(url, currency, dateZacetni, dateKoncni);

            tableFunction(rates);
            graphFunction(rates);
            calculateFunction(rates, dateZacetni, dateKoncni);

            JOptionPane.showMessageDialog(null,"Odpiram graf, kalkulacijo in tabelo."
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null,"Napaka:\n" + e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static List<ExchangeRate> readerFunction(String xmlUrl, String[] currency, String dateZacetni, String dateKoncni) throws Exception {
        List<ExchangeRate> rates = new ArrayList<>();
        URL url = new URL(xmlUrl);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(url.openStream());

        NodeList rows = doc.getElementsByTagName("tecajnica");
        for (int i = 0; i < rows.getLength(); i++) {
            Node node = rows.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element row = (Element) node;
                String date = row.getAttribute("datum");
                NodeList currencies = row.getElementsByTagName("tecaj");
                for (int j = 0; j < currencies.getLength(); j++) {
                    Element c = (Element) currencies.item(j);
                    for (String s : currency) {
                        if (s.equals(c.getAttribute("oznaka")) && date.compareTo(dateZacetni) >= 0 && date.compareTo(dateKoncni) <= 0) {
                            double rate = Double.parseDouble(c.getTextContent());
                            rates.add(new ExchangeRate(date, s, rate));
                        }
                    }
                }
            }
        }
        return rates;
    }

    public static void tableFunction(List<ExchangeRate> rates) {

        String[] columns = {"Datum", "Valuta", "Tečaj"};

        String[][] data = new String[rates.size()][3];

        for (int i = 0; i < rates.size(); i++) {
            ExchangeRate r = rates.get(i);
            data[i][0] = r.date;
            data[i][1] = r.currency;
            data[i][2] = String.valueOf(r.rate);
        }

        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame frame = new JFrame("Tabela tečajev");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    public static void calculateFunction(List<ExchangeRate> rates, String dateZacetni, String dateKoncni) {

        List<String> currencies = new ArrayList<>();

        for (ExchangeRate r : rates) {
            if (!currencies.contains(r.currency)) {
                currencies.add(r.currency);
            }
        }

        String[] columns = {"Valuta", "Začetni", "Končni", "Sprememba (%)"};
        String[][] data = new String[currencies.size()][4];

        int i = 0;

        for (String currency : currencies) {

            ExchangeRate first = null;
            ExchangeRate last = null;

            for (ExchangeRate r : rates) {

                if (!r.currency.equals(currency)) continue;

                if (first == null) first = r;
                last = r;
            }

            if (first != null && last != null) {

                double change =((last.rate - first.rate) / first.rate) * 100;

                data[i][0] = currency;
                data[i][1] = String.format("%.4f", first.rate);
                data[i][2] = String.format("%.4f", last.rate);
                data[i][3] = String.format("%.2f %%", change);

            } else {
                data[i][0] = currency;
                data[i][1] = "N/A";
                data[i][2] = "N/A";
                data[i][3] = "N/A";
            }

            i++;
        }

        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame frame = new JFrame("Izračun spremembe tečaja");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    public static void graphFunction(List<ExchangeRate> rates) {

        JFrame frame = new JFrame("Graf");
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int margin = 50;

        frame.add(new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int width = getWidth();
                int height = getHeight();

                //axis
                g.drawLine(margin, height - margin, width - margin, height - margin);
                g.drawLine(margin, margin, margin, height - margin);

                double min = rates.stream().mapToDouble(r -> r.rate).min().orElse(0);
                double max = rates.stream().mapToDouble(r -> r.rate).max().orElse(1);

                List<String> currencies = new ArrayList<>();

                for (ExchangeRate r : rates) {
                    if (!currencies.contains(r.currency)) {
                        currencies.add(r.currency);
                    }
                }

                for (int c = 0; c < currencies.size(); c++) {

                    String currency = currencies.get(c);
                    switch(c) {
                        case 0:
                            g.setColor(Color.RED);
                            break;
                        case 1:
                            g.setColor(Color.BLUE);
                            break;
                        default:
                            g.setColor(Color.BLACK);
                    }

                    ExchangeRate previous = null;
                    int pointIndex = 0;

                    for (ExchangeRate current : rates) {

                        if (!current.currency.equals(currency)) {
                            continue;
                        }

                        if (previous != null) {

                            int x1 = margin + (pointIndex - 1) * (width - margin*2)
                                    / (rates.size() / currencies.size());

                            int x2 = margin + pointIndex * (width - margin*2)
                                    / (rates.size() / currencies.size());

                            int y1 = (int) (height - margin -
                                    ((previous.rate - min) / (max - min))
                                            * (height - margin*2));

                            int y2 = (int) (height - margin -
                                    ((current.rate - min) / (max - min))
                                            * (height - margin*2));

                            g.drawLine(x1, y1, x2, y2);
                        }

                        previous = current;
                        pointIndex++;
                    }

                    // legenda
                    g.drawString(currency, width - 120, 30 + c * 20);
                }
                g.setColor(Color.BLACK);
                g.drawString("Tečaj", margin/2, margin/2);
                g.drawString("Datum", width-(margin*2), height-(margin/2));
            }
        });

        frame.setVisible(true);
    }
}