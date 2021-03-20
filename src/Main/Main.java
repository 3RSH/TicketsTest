package Main;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {

  private static final String ORIGIN_NAME = "Владивосток";
  private static final String DESTINATION_NAME = "Тель-Авив";

  private static final List<Integer> flyDurations = new ArrayList<>();

  public static void main(String[] args) {
    parseTicketsJSON(args[0]);

    System.out.println("Среднее время полета между городами "
        + ORIGIN_NAME + " и " + DESTINATION_NAME + " - "
        + formatDuration(getAverageDuration()));

    System.out.println("90-й процентиль времени полета между городами "
        + ORIGIN_NAME + " и " + DESTINATION_NAME + " - "
        + formatDuration(getPercentile(90)));
  }

  //Парсинг JSON-файла с авиа-билетами -> заполнение массива flyDurations
  private static void parseTicketsJSON(String path) {
    try (InputStreamReader reader = new InputStreamReader(
        new FileInputStream(path), StandardCharsets.UTF_8)) {

      //кодировка UTF-8 with BOM -> пропускаем 1-й символ
      //(код 65279, неразрывный пробел нулевой ширины),
      //для корректного парсинга
      reader.skip(1);

      JSONParser parser = new JSONParser();
      JSONObject jsonObject = (JSONObject) parser.parse(reader);
      JSONArray tickets = (JSONArray) jsonObject.get("tickets");

      SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");

      for (Object ticket : tickets) {

        String originName = ((JSONObject) ticket).get("origin_name").toString();
        String destinationName = ((JSONObject) ticket).get("destination_name").toString();

        if (originName.equals(ORIGIN_NAME) && destinationName.equals(DESTINATION_NAME)) {
          String departureDateStr = ((JSONObject) ticket).get("departure_date").toString();
          departureDateStr += " " + ((JSONObject) ticket).get("departure_time").toString();

          String arrivalDateStr = ((JSONObject) ticket).get("arrival_date").toString();
          arrivalDateStr += " " + ((JSONObject) ticket).get("arrival_time").toString();

          Date departureDate = dateFormat.parse(departureDateStr);
          Date arrivalDate = dateFormat.parse(arrivalDateStr);

          flyDurations.add((int) ((arrivalDate.getTime() - departureDate.getTime()) / 60000));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Получение среднего времени полёта из массива flyDurations
  private static int getAverageDuration() {
    if (flyDurations.size() > 0) {
      return flyDurations.stream().mapToInt(d -> d).sum() / flyDurations.size();
    }
    return 0;
  }

  //Получение указанного процентиля времени полёта из массива flyDurations
  private static int getPercentile(int percent) {
    for (int i = 0; i < flyDurations.size(); i++) {
      int count = 0;
      int duration = flyDurations.get(i);

      for (int flyDuration : flyDurations) {
        if (duration >= flyDuration) {
          count++;
        }
      }

      if (count * 100 / flyDurations.size() == percent) {
        return duration;
      }
    }
    return 0;
  }

  //Форматирование времени времени полёта в формат HH:mm
  private static String formatDuration(int duration) {
    int hours = duration / 60;
    int minutes = duration - hours * 60;

    return hours + ":" + minutes;
  }
}
