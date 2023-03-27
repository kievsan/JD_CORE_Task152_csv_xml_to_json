import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvBeanIntrospectionException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Employee {
    // toCSV, fromCSV
    // toXML, fromXML
    // toJSON, from JSON

    public long id;
    public String firstName;
    public String lastName;
    public String country;
    public int age;


    public Employee() {
        // для парсинга Java классов из CSV потребуется пустой конструктор класса
    }


    public Employee(long id, String firstName, String lastName, String country, int age) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.age = age;
    }


    @Override
    public String toString() {
        return "\nEmployee{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", country='" + country + '\'' +
                ", age='" + age + '\'' +
                "}";
    }


    static List<Employee> fromCSV(String fileName) {
        printReadingHead(fileName);
        List<Employee> staff = new ArrayList<>();

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File " + fileName + " not found");
            return staff;
        }

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();   // сопоставляем
            strategy.setType(Employee.class);                                           // классу <Employee> CVS файл
            strategy.setColumnMapping("id", "firstName", "lastName", "country", "age"); // поколоночно;
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)    // назначим csv-строителя
                    .withMappingStrategy(strategy)                              // для reader и <Employee>;
                    .build();
            staff = csv.parse();                                                       // строим объекты;
            printLog(fileName, staff, "read from a file");
        } catch (IOException | CsvBeanIntrospectionException e) {
            e.printStackTrace();
        }
        return staff;
    }


    public static void toCSV(String fileName, List<Employee> staff) {
        printWritingHead(fileName, staff);
        delFile(fileName);

        ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();   // сопоставляем
        strategy.setType(Employee.class);                                           // классу <Employee> CVS файл
        strategy.setColumnMapping("id", "firstName", "lastName", "country", "age"); // поколоночно;
        try (Writer writer = new FileWriter(fileName)) {
            StatefulBeanToCsv<Employee> to_csv = new StatefulBeanToCsvBuilder<Employee>(writer)
                    .withMappingStrategy(strategy)
                    .build();
            to_csv.write(staff);
            printLog(fileName, staff, "written to a file");
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            e.printStackTrace();
        }
    }


    public static List<Employee> fromXML(String fileName)
            throws ParserConfigurationException, IOException, SAXException {
        printReadingHead(fileName);
        List<Employee> staff = new ArrayList<>();

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File " + fileName + " not found");
            return staff;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse( new File(fileName));
        Node root = doc.getDocumentElement();

        staff = readXML(root, staff);
        printLog(fileName, staff, "read from a file");
        return staff;
    }





    public static List<Employee> readXML(Node node, List<Employee> staff) {
        Employee employee = new Employee();
        NodeList nodeList = node.getChildNodes();

        if (!(Node.ELEMENT_NODE == node.getNodeType() && node.getNodeName().equals("employee"))) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node_ = nodeList.item(i);
                if (Node.ELEMENT_NODE == node_.getNodeType()) {
                    if (node_.getNodeName().equals("employee")) {
                        readXML(node_, staff);
                        return staff;
                    }
                    System.out.println("Skipped node: " + node_.getNodeName());
                }
            }
            return staff;
        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node_ = nodeList.item(i);

            if (!(node_.hasChildNodes() && Node.TEXT_NODE == node_.getFirstChild().getNodeType())) {
                System.out.println("skipped node: " + node_.getNodeName());
                continue; }

            Node field = node_.getFirstChild();
            String node_name = node_.getNodeName();

            switch (node_name) {
                case "id" -> {
                    employee.id = Long.parseLong(field.getNodeValue());
                }
                case "firstName" -> {
                    employee.firstName = field.getNodeValue();
                }
                case "lastName" -> {
                    employee.lastName = field.getNodeValue();
                }
                case "country" -> {
                    employee.country = field.getNodeValue();
                }
                case "age" -> {
                    employee.age = Integer.parseInt(field.getNodeValue());
                }
                default -> {
                    System. out.println("unexpected field");
                }
            }

        }

        if (employee.id > 0) {
            staff.add(employee); };

        if (node.getNextSibling() != null) {
            readXML(node.getNextSibling(), staff); }

        return staff;
    }


    public static void toXML(String fileName, List<Employee> staff)
            throws ParserConfigurationException, TransformerException {
        printWritingHead(fileName, staff);
        delFile(fileName);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        org.w3c.dom.Element root = document.createElement("root");
        document.appendChild(root);

        for (Employee one : staff) {

            org.w3c.dom.Element employee = document.createElement("employee");
            root.appendChild(employee);

            org.w3c.dom.Element id = document.createElement("id");
            id.appendChild(document.createTextNode(String.valueOf(one.id)));
            employee.appendChild(id);

            org.w3c.dom.Element firstName = document.createElement("firstName");
            firstName.appendChild(document.createTextNode(String.valueOf(one.firstName)));
            employee.appendChild(firstName);

            org.w3c.dom.Element lastName = document.createElement("lastName");
            lastName.appendChild(document.createTextNode(String.valueOf(one.lastName)));
            employee.appendChild(lastName);

            org.w3c.dom.Element country = document.createElement("country");
            country.appendChild(document.createTextNode(String.valueOf(one.country)));
            employee.appendChild(country);

            org.w3c.dom.Element age = document.createElement("age");
            age.appendChild(document.createTextNode(String.valueOf(one.age)));
            employee.appendChild(age);
        }

        DOMSource domSource = new DOMSource((Node) document);
        StreamResult streamResult = new StreamResult( new File(fileName));
        TransformerFactory transformerFactory = TransformerFactory. newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(domSource, streamResult);

        printLog(fileName, staff, "written to a file");
    }


    private static void printLog(String fileName, List<Employee> staff, String service) {
        System.out.println(staff.size() + " " + staff.get(0).getClass().getName() + " " +
                " read from a file " + " " + fileName.toUpperCase(Locale.ROOT) + "\tSUCCESS");
    }

    private static void printWritingHead(String fileName, List<Employee> staff) {
        System.out.println("\n -> writing list of " + staff.get(0).getClass().getName() +
                " to a file: " + fileName.toUpperCase(Locale.ROOT));
    }

    private static void printReadingHead(String fileName) {
        System.out.println("\n -> reading from a file: " + fileName.toUpperCase(Locale.ROOT));
    }

    private static void delFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File " + fileName + " deleted");
            }
        } else {
            System.out.println("File " + fileName + " not found");
        }
    }

}
