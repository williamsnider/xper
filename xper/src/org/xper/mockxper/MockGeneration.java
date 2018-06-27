package org.xper.mockxper;

import org.xper.db.vo.GenerationInfo;
import org.xper.db.vo.GenerationTaskToDoList;
import org.xper.util.DbUtil;

public class MockGeneration {
	GenerationInfo genInfo;
	DbUtil dbUtil;
	
	public MockGeneration (GenerationInfo genDef) {
		this.genInfo = genDef;
	}
	
	public GenerationTaskToDoList getTaskToDoList (){
		return dbUtil.readTaskToDoByGeneration(genInfo.getGenId(),genInfo.getLinId());
	}
	
	
	public void setDbUtil(DbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}

	public GenerationInfo getGenDef() {
		return genInfo;
	}
}
