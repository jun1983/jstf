package com.jstf.selenium;

import org.openqa.selenium.By;

import lombok.Data;

@Data
public class QSelector {
	private By by;
	private ElementSelectionType elementSelectionType;
	
	public QSelector(By by, ElementSelectionType elementSelectionType) {
		this.by = by;
		this.elementSelectionType = elementSelectionType;
	}
}

