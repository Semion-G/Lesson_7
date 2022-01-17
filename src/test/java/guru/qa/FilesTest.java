package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Configuration;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesTest {

    @BeforeAll
    static void beforeAll() {
        Configuration.startMaximized = true;
    }

    @Test
    @DisplayName("Проверка отображения имени файла после загрузки")
    void fileNameIsDisplayedAfterUploadTest() {
        open("https://demoqa.com/upload-download");
        $("#uploadFile").uploadFromClasspath("TestTxtFile.txt");
        $("#uploadedFilePath").shouldHave(text("TestTxtFile.txt"));
    }

    @Test
    @DisplayName("Скачанный txt файл содержит ожидаемый текст")
    void downloadedTextFileContainsCorrectText() throws IOException {
        open("https://litportal.ru/avtory/chingiz-abdullaev/kniga-prays-list-dlya-izdatelya-174344.html");
        File file = $(".btn_download").find(byText("Скачать txt")).download();
        String fileContent = IOUtils.toString(new FileReader(file));
        assertTrue(fileContent.contains("Прайс-лист для издателя"));
    }

    @Test
    @DisplayName("Скачанный pdf файл содержит ожидаемое число страниц")
    void downloadedPdfFileContainsCorrectNumberOfPages() throws IOException {
        open("http://kvm.gubkin.ru/");
        File pdf = $(byText("Расписание пересдач")).parent().download();
        PDF parsedPDF = new PDF(pdf);
        assertEquals(1, parsedPDF.numberOfPages);
    }

    @Test
    @DisplayName("Скачивание xls файла и проверка его содержимого")
    void downloadedXlsFileContainsCorrectRaws() throws IOException {
        open("https://kub-24.ru/schet-na-oplatu/");
        File xls = $(byText("Скачать прайс-лист в формате Microsoft Excel")).download();
        XLS parsedXLS = new XLS(xls);
        boolean checkPassed = parsedXLS.excel
                .getSheetAt(0)
                .getRow(7)
                .getCell(1)
                .getStringCellValue()
                .contains("Наш адрес: 177405 Москва, Дорожная ул., д. 60, офис 1, м. «Аннино»");

        assertTrue(checkPassed);
    }

    @Test
    @DisplayName("Парсинг CSV файлов")
    void parseCsvFileTest() throws IOException, CsvException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("test.csv");
             Reader reader = new InputStreamReader(is)) {
            CSVReader csvReader = new CSVReader(reader);

            List<String[]> strings = csvReader.readAll();
            assertEquals(2, strings.size());
        }
    }

    @Test
    @DisplayName("Парсинг ZIP файлов")
    void parseZipFileTest() throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("TestDZ.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println(entry.getName());
                assertThat(zis.getNextEntry().getName().contains("tb3(1).pdf"));
            }
        }
    }
}
