package com.juntools.selenium;

import org.openqa.selenium.By;

import lombok.Data;

@Data
public class JSelector {
	private By by;
	private ElementSelectionType elementSelectionType;
	
	public JSelector(By by, ElementSelectionType elementSelectionType) {
		this.by = by;
		this.elementSelectionType = elementSelectionType;
	}
}

