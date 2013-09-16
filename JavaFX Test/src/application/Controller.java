package application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class Controller {

	@FXML
	private TextField input;
	@FXML
	private Slider slider;
	@FXML
	private Label labelCityName;

	public void initialize() {
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
					input.setText(newValue.intValue()+"");
			}
		});
		input.setText(Math.round(slider.getValue())+"");
	}

	@FXML
	protected void buttonOnClick(ActionEvent event) {
		input.setText(slider.getValue() + "");
	}

	@FXML
	protected void dragDone(ActionEvent event) {
		input.setText(slider.getValue() + "");
	}
	
	@FXML
	protected void onGermanyClicked(MouseEvent event) {
		labelCityName.setText("Deutschland");
	}
	
	@FXML
	protected void onUSAClicked(MouseEvent event) {
		labelCityName.setText("USA");
	}
	
}
