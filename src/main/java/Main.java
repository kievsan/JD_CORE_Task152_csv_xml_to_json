import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Main {

    static final String BASE_FILE_NAME = "staff";


    public static void main(String[] args)
            throws IOException, ParserConfigurationException, TransformerException, SAXException {

        Locale.setDefault(new Locale("ru", "RU", ""));

        List<Employee> staff, employees;
        employees = List.of(
                new Employee(1,"John","Smith","USA",25),
                new Employee(2,"Inav","Petrov","RU",23));

        Employee.toCSV(BASE_FILE_NAME + ".csv", employees);
        Employee.toXML(BASE_FILE_NAME + ".xml", employees);

        staff = Employee.fromCSV(BASE_FILE_NAME + ".csv");
        System.out.println(staff);

        staff = Employee.fromXML(BASE_FILE_NAME + ".xml");
        System.out.println(staff);


    }


}
