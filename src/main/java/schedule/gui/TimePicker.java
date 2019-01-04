package schedule.gui;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalTime;

public class TimePicker extends Spinner<LocalTime> {

    private TimeSpinnerValueFactory valueFactory = new TimeSpinnerValueFactory();

    public TimePicker() {
        setValueFactory(valueFactory);
        getEditor().setOnMouseClicked(e -> valueFactory.change());
    }

    public void setValue(LocalTime time) {
        valueFactory.reset();
        valueFactory.setValue(time);
    }

    private static class TimeSpinnerValueFactory extends SpinnerValueFactory<LocalTime> {

        private int minutesPerStep = 1;

        TimeSpinnerValueFactory() {
            setConverter(new LocalTimeStringConverter());
            setValue(LocalTime.now());
        }

        @Override
        public void decrement(int steps) {
            setValue(getValue().minusMinutes(minutesPerStep * steps));
        }

        @Override
        public void increment(int steps) {
            setValue(getValue().plusMinutes(minutesPerStep * steps));
        }

        void change() {
            if (minutesPerStep == 1) {
                minutesPerStep = 60;
            } else if (minutesPerStep == 60) {
                minutesPerStep = 1;
            }
        }

        void reset() {
            minutesPerStep = 1;
        }

    }

}
