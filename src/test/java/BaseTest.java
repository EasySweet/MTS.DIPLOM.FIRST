import com.codeborne.selenide.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
import static org.openqa.selenium.devtools.v129.network.Network.clearBrowserCache;

public class BaseTest {

    @BeforeEach
    public void setup() {
        Configuration.baseUrl = "https://www.mtsbank.ru";
        Configuration.browserSize = "1920x1080";
        Configuration.headless = true; // Можно использовать без графического интерфейса для ускорения
    }

    @AfterEach
    void tearDown() {
        // Очистка кэша и закрытие браузера после каждого теста
        clearBrowserCache();
        Selenide.closeWebDriver();
    }


        @Test
        public void PhoneCheck() {
            // Открываем главную страницу
            open("/");

            // Наводим курсор на кнопку
            $(".styled__Button-sc-df372149-1.bQavBi").hover();

            // Кликаем по ссылке
            $(".styled__Link-sc-df372149-4.hteRSU").click();

            // Кликаем по полю ввода телефона и вводим номер
            $("#phone").setValue("1111111111");

            //потом
            $(".sc-eCssSg.imKPPu").click();

            //потом х2
            String errorMessage = $("[data-testid='error']").shouldBe(visible).getText();
            System.out.println("Ошибка: " + errorMessage);

        }


    @Test
    public void testNavigateAndSelectRandomTab() {
        // Открываем главную страницу
        open("/");

        // Кликаем по ссылке на карты
        $("[href='/chastnim-licam/karti/']").click();

        Actions actions = actions(); // Получение объекта Actions
        actions.moveByOffset(50, 50).perform(); // Перемещение на координаты (50, 50) от текущего положения

        // Получение коллекции элементов с определённым XPATH
        ElementsCollection tabs = $$x("//a[@data-testid='tab']");

        // Фильтрация вкладок "Кредитные" или "Дебетовые"
        ElementsCollection creditTab = tabs.filterBy(text("Кредитные"));
        ElementsCollection debitTab = tabs.filterBy(text("Дебетовые"));

        // Проверка наличия подходящих вкладок
        if (creditTab.isEmpty() && debitTab.isEmpty()) {
            System.out.println("Не найдены подходящие вкладки.");
            return;
        }

        // Случайный выбор вкладки
        Random random = new Random();
        boolean isCreditSelected = random.nextBoolean();
        SelenideElement selectedTab = isCreditSelected
                ? creditTab.first()  // Выбор "Кредитные"
                : debitTab.first();  // Выбор "Дебетовые"

        // Клик по выбранной вкладке
        selectedTab.click();

        // Проверка элементов на странице
        String expectedWord = isCreditSelected ? "кредитная" : "дебетовая";
        ElementsCollection headings = $$x("//div[@class='Row-sc-a26adb6c-0 gfbyqz'][@data-testid='heading']");

        boolean allMatch = headings.stream()
                .allMatch(heading -> heading.getText().toLowerCase().contains(expectedWord.toLowerCase()));

        // Результат проверки
        if (allMatch) {
            System.out.println("Все элементы содержат слово \"" + expectedWord + "\".");
        } else {
            throw new AssertionError("Не все элементы содержат слово \"" + expectedWord + "\".");
        }


    }


    @Test
    public void VkladTest() {
        // Открываем главную страницу
        open("/");
        // Кликаем по ссылке на карты
        $("[href='/chastnim-licam/vkladi/']").click();

        //Меняем положение курсора
        Actions actions = actions(); // Получение объекта Actions
        actions.moveByOffset(50, 50).perform(); // Перемещение на координаты (50, 50) от текущего положения

        // Находим контейнер с классом "sc-cBNeex ifyjLT"
        SelenideElement container = $(".sc-cBNeex.ifyjLT");

        // Находим список элементов с классом "sc-lmoMya mapFK"
        ElementsCollection elements = container.$$x(".//div[contains(@class, 'sc-lmoMya mapFK')]");

        // Проверяем, что есть доступные элементы
        if (elements.isEmpty()) {
            throw new AssertionError("Не найдено ни одного элемента с классом 'sc-lmoMya mapFK'");
        }

        // Случайный выбор элемента
        Random random = new Random();
        SelenideElement randomElement = elements.get(random.nextInt(elements.size()));

// Находим ссылку внутри выбранного элемента
        SelenideElement linkElement = randomElement.$("a[href]");
        String linkHref = linkElement.getAttribute("href");
        System.out.println("Ссылка выбранного элемента: " + linkHref);

// Получаем процент из текста внутри randomElement
        SelenideElement textElement = randomElement.$(".styled__SmartText-n9vm43-0.hTnjkD");
        String percentText = textElement.getText().replaceAll("[^0-9]", ""); // Убираем всё, кроме цифр
        System.out.println("Процент выбранного элемента: " + percentText);

// Проверяем, если процент равен 23
        if (percentText.equals("23")) {
            System.out.println("Данный вклад модульный и может изменяться. Дальнейшие проверки и клики не выполняются.");
            return; // Завершаем выполнение метода
        }

// Кликаем по ссылке
        linkElement.click();

// Ожидаем появления элемента h3 или h4 с текстом "Доходность до"
        SelenideElement headingElement = null;

        try {
            // Пытаемся найти элемент <h3>
            headingElement = $("h3[data-testid='heading']");
            headingElement.should(appear);  // Ожидаем, что элемент появится на странице
        } catch (Exception e) {
            // Если <h3> не найден, ищем <h4>
            headingElement = $("h4[data-testid='heading']");
            headingElement.should(appear);  // Ожидаем, что элемент появится на странице
        }

// Извлекаем текст из заголовка
        String headingText = headingElement.getText();
        System.out.println("Текст в заголовке: " + headingText);

// Извлекаем процентное число из текста заголовка (перед символом %)
        String foundPercentText = headingText.replaceAll("[^0-9]", ""); // Оставляем только цифры
        System.out.println("Процент на новой странице: " + foundPercentText);

// Преобразуем строку в число и выводим без запятой (целое число)
        int foundPercent = Integer.parseInt(foundPercentText);
        System.out.println("Процент на новой странице (целое число): " + foundPercent);

// Сравниваем процент с выбранным элементом
        if (percentText.equals(foundPercentText)) {
            System.out.println("Процент совпал!");
        } else {
            System.out.println("Процент не совпал.");
        }
    }


    @Test
    public void CheckCurrentNumberFilling() {
        // Открываем главную страницу
        open("/");

        // Работа с первым элементом
        SelenideElement firstLinkElement = $("a[data-testid='link'][href='/chastnim-licam/krediti/']");

        firstLinkElement.shouldBe(Condition.visible); // Убеждаемся, что элемент видим

        Actions actions = actions(); // Создаем объект Actions

        actions.moveToElement(firstLinkElement).perform(); // Наводим курсор на элемент

        // Работа со вторым элементом
        SelenideElement secondLinkElement = $("a[data-testid='link'][href='/chastnim-licam/krediti/credit-all/?scroll=loanCalc']");

        secondLinkElement.shouldBe(Condition.visible); // Убеждаемся, что элемент видим

        actions.moveToElement(secondLinkElement).perform(); // Наводим курсор на элемент

        secondLinkElement.click(); // Кликаем на элемент

        sleep(3000); // Ждём 3 секунды для проверки автоматической центрации страницы на поле для ввода расчета кредита

        // Работа с полем ввода
        SelenideElement inputField = $("input[data-testid='input-slider']");

        // Убеждаемся, что элемент видим и доступен для взаимодействия
        inputField.shouldBe(Condition.visible).shouldBe(Condition.enabled);

        // Кликаем по элементу
        inputField.click();

        // Вводим число 1111111111
        inputField.setValue("1111111111");

        // Получаем значение из поля ввода и выводим его в консоль
        String inputValue = inputField.getValue();
        System.out.println("Значение в поле ввода: " + inputValue);

        // Кликаем по элементу выше по координатам (например, перемещаем курсор на 0, 0 пикселей)
        actions.moveByOffset(0, 0).click().perform();

        sleep(1000); // Пауза

        // Повторно кликаем по полю ввода
        inputField.click();

        // Проверяем текущее значение в поле и выводим его в консоль
        String finalInputValue = inputField.getValue();
        System.out.println("Текущее значение в поле ввода после повторного клика: " + finalInputValue);
    }

    @Test
    public void VkladAgain() {
        // Открываем главную страницу
        open("/");
        // Работа с первым элементом
        SelenideElement firstLinkElement = $("a[data-testid='link'][href='/chastnim-licam/vkladi/']");

        firstLinkElement.shouldBe(Condition.visible); // Убеждаемся, что элемент видим

        Actions actions = actions(); // Создаем объект Actions

        actions.moveToElement(firstLinkElement).perform(); // Наводим курсор на элемент

        // Работа со вторым элементом
        SelenideElement secondLinkElement = $("a[data-testid='link'][href='/chastnim-licam/vkladi/mts-vklad/']");

        secondLinkElement.shouldBe(Condition.visible); // Убеждаемся, что элемент видим

        actions.moveToElement(secondLinkElement).perform(); // Наводим курсор на элемент

        secondLinkElement.click(); // Кликаем на элемент

        sleep(2000); // ждём прогрузку калькулятора

        // Работа с полем ввода
// Работа с полем ввода
        SelenideElement inputField = $("input[data-testid='input-slider'][name='amount']");

        // Прокручиваем страницу до поля ввода и немного дальше
        inputField.scrollIntoView(false);  // Прокручиваем страницу до элемента

        // Прокручиваем на небольшое расстояние с помощью executeJavaScript
        executeJavaScript("window.scrollBy(0, 200);");  // Дополнительная прокрутка вниз

        // Убеждаемся, что поле ввода видимо
        inputField.shouldBe(Condition.visible);

        // Выделяем весь текст в поле
        inputField.sendKeys(Keys.CONTROL, "a");  // Для Windows/Linux используем CTRL+A, для Mac CMD+A

        sleep(300); // Пауза, чтобы увидеть, что текст выделен

        // Стираем выделенный текст с помощью клавиши Backspace
        inputField.sendKeys(Keys.BACK_SPACE);

// Генерация случайного числа от 10000 до 500000000
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(490000001); // Диапазон от 10000 до 500000000

// Вводим сгенерированное случайное число в поле
        inputField.setValue(String.valueOf(randomNumber));

// Печатаем в консоль введённое число
        System.out.println("Сумма вклада (Введенное случайное число): " + randomNumber);

// Извлекаем значение из элемента с указанным классом
        SelenideElement valueElement = $("div.sc-1c576a46-0.kSfuJG");

// Получаем текст из элемента
        String valueText = valueElement.text();

// Печатаем значение, которое найдено в div
        System.out.println("Значение из div.sc-1c576a46-0.kSfuJG: " + valueText);

// Очищаем текст от ненужных символов (оставляем только цифры и запятую)
        String valueWithoutComma = valueText.replaceAll("[^0-9,]", "");

// Убираем все пробелы между цифрами
        String cleanedValueWithoutSpaces = valueWithoutComma.replaceAll(" ", "");

// Если в строке есть запятая, мы оставляем только часть до запятой
        String numberBeforeCommaFromValueText = cleanedValueWithoutSpaces.split(",")[0];

// Печатаем число до запятой из valueText
        System.out.println("Число до запятой из valueText: " + numberBeforeCommaFromValueText);

// Извлекаем значение из другого элемента (div с классом Wrapper-sc-1vydk7-0 edTxiO)
        SelenideElement valueFromWrapper = $("div.Wrapper-sc-1vydk7-0.edTxiO");

// Получаем текст из элемента
        String wrapperText = valueFromWrapper.text();

// Печатаем значение, которое найдено в div с классом Wrapper-sc-1vydk7-0.edTxiO
        System.out.println("Значение из Дохода по вкладу: " + wrapperText);

// Очищаем текст от ненужных символов, оставляем только цифры и запятую
        String cleanedWrapperText = wrapperText.replaceAll("[^0-9,]", "");

// Убираем все пробелы между цифрами
        String cleanedWrapperTextWithoutSpaces = cleanedWrapperText.replaceAll(" ", "");

// Если в строке есть запятая, мы оставляем только часть до запятой
        String numberBeforeCommaFromWrapper = cleanedWrapperTextWithoutSpaces.split(",")[0];

// Печатаем число до запятой из wrapperText
        System.out.println("Число до запятой из wrapperText: " + numberBeforeCommaFromWrapper);

// Переводим numberBeforeCommaFromValueText в число и выводим в консоль
        long valueNumber = Long.parseLong(numberBeforeCommaFromValueText);
        System.out.println("Число число из valueText (переведённое в long): " + valueNumber);

// Переводим numberBeforeCommaFromWrapper в число и выводим в консоль
        long wrapperNumber = Long.parseLong(numberBeforeCommaFromWrapper);
        System.out.println("Число число из wrapperText (переведённое в long): " + wrapperNumber);

        // Переводим randomNumber в long
        long randomNumberLong = (long) randomNumber;

// Вычисляем результат, вычитая randomNumber из valueNumber
        long result = valueNumber - randomNumberLong;

// Выводим результат в консоль
        System.out.println("Результат вычитания: " + result);


// Сравниваем результат с wrapperNumber
        if (result == wrapperNumber) {
            System.out.println("Результат и wrapperNumber совпадают.");
        } else {
            System.out.println("Результат и wrapperNumber не совпадают.");
        }
    }
    }














