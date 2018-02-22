package org.xper.png.util;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.xper.db.vo.GenerationTaskDoneList;
import org.xper.db.vo.StimSpecEntry;
import org.xper.db.vo.TaskDoneEntry;
import org.xper.util.DbUtil;


public class PngDbUtil extends DbUtil {
	public PngDbUtil() {	
		super();
	}
	
	public PngDbUtil(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public long getStimIdByTaskId(long paramLong) {
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);
		Map<String, Object> localMap = jt.queryForMap(" select t.stim_id as id from TaskToDo t where t.task_id = ?", new Object[] { new Long(paramLong) });
		
		long l = ((Long)localMap.get("id")).longValue();
		return l;
	}
	
	public void writeStimObjData(long id, String javaspec, String mstickspec, String blenderspec, String data) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		jt.update("insert into StimObjData (id, javaspec, mstickspec, blenderspec dataspec) values (?, ?, ?)", 
				new Object[] { id, javaspec, mstickspec, blenderspec, data });
	}
	
	public void updateStimObjData(long id, String data) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		jt.update("update StimObjData set dataspec = ? where id = ?", 
				new Object[] { data, id });
	}
	
	public StimSpecEntry readStimSpec_java(long stimObjId) {
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);
		return jt.queryForObject(
				" select id, javaspec from StimObjData where id = ? ", 
				new ParameterizedRowMapper<StimSpecEntry> () {
					public StimSpecEntry mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						StimSpecEntry ent = new StimSpecEntry();

						ent.setStimId(rs.getLong("id")); 
						ent.setSpec(rs.getString("javaspec")); 

						return ent;
					}},
				stimObjId);
	}

	public StimSpecEntry readStimSpec_stick(long stimObjId) {
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);
		return jt.queryForObject(
				" select id, mstickspec from StimObjData where id = ? ", 
				new ParameterizedRowMapper<StimSpecEntry> () {
					public StimSpecEntry mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						StimSpecEntry ent = new StimSpecEntry();

						ent.setStimId(rs.getLong("id")); 
						ent.setSpec(rs.getString("mstickspec")); 

						return ent;
					}},
				stimObjId);
	}
	
	public StimSpecEntry readStimSpec_blender(long stimObjId) {
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);
		return jt.queryForObject(
				" select id, blenderspec from StimObjData where id = ? ", 
				new ParameterizedRowMapper<StimSpecEntry> () {
					public StimSpecEntry mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						StimSpecEntry ent = new StimSpecEntry();

						ent.setStimId(rs.getLong("id")); 
						ent.setSpec(rs.getString("blenderspec")); 

						return ent;
					}},
				stimObjId);
	}
	
	public StimSpecEntry readStimSpec_data(long stimObjId) {
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);
		return jt.queryForObject(
				" select id, dataspec from StimObjData where id = ? ", 
				new ParameterizedRowMapper<StimSpecEntry> () {
					public StimSpecEntry mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						StimSpecEntry ent = new StimSpecEntry();

						ent.setStimId(rs.getLong("id")); 
						ent.setSpec(rs.getString("dataspec")); 

						return ent;
					}},
				stimObjId);
	}
	
	public long readStimObjIdFromDescriptiveId(String descriptiveId) {
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);
		return jt.queryForLong("SELECT id from StimObjData where descId = ?", new Object[] { new String(descriptiveId) });
	}
	
	public String readDescriptiveIdFromStimObjId(long stimObjId) {
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);
		return jt.queryForObject("SELECT descId from StimObjData where id = ?",String.class, stimObjId);
	}
	
	public String readCurrentDescriptivePrefix() {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		long tstamp = jt.queryForLong("SELECT max(tstamp) FROM DescriptiveInfo");
		int gaRun = jt.queryForInt("SELECT gaRun FROM DescriptiveInfo WHERE tstamp = ? ", new Object[] { new Long(tstamp) });
		Long cEI = jt.queryForLong("SELECT prefix FROM DescriptiveInfo WHERE tstamp = ? ", new Object[] { new Long(tstamp) });
		
		return new String(cEI.toString() + "_r-" + gaRun);
	}
	
	public String readCurrentDescriptivePrefixAndGen() {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		long tstamp = jt.queryForLong("SELECT max(tstamp) FROM DescriptiveInfo");
		int gaRun = jt.queryForInt("SELECT gaRun FROM DescriptiveInfo WHERE tstamp = ? ", new Object[] { new Long(tstamp) });
		Long cEI = jt.queryForLong("SELECT prefix FROM DescriptiveInfo WHERE tstamp = ? ", new Object[] { new Long(tstamp) });
		Long genNum = jt.queryForLong("SELECT genNum FROM DescriptiveInfo WHERE tstamp = ? ", new Object[] { new Long(tstamp) });
		
		return new String(cEI.toString() + "_r-" + gaRun + "_g-" + genNum);
	}
	
	public void writeDescriptiveFirstTrial(Long id) {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        long tstamp = jt.queryForLong("SELECT max(tstamp) FROM DescriptiveInfo");
        
        jt.update("update DescriptiveInfo set firstTrial = ? where tstamp = ?", 
            new Object[] { id, tstamp });
    }
    
    public void writeDescriptiveLastTrial(Long id) {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        long tstamp = jt.queryForLong("SELECT max(tstamp) FROM DescriptiveInfo");
        
        jt.update("update DescriptiveInfo set lastTrial = ? where tstamp = ?", 
                new Object[] { id, tstamp });
    }
	
	public void updateJavaSpec(long id, String javaSpec) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		jt.update("update StimObjData set javaspec = ? where id = ?", 
				new Object[] { javaSpec, id });
	}
	
	public void writeMStickSpec(long id, String mStickSpec) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		jt.update("update StimObjData set mstickspec = ? where id = ?", 
				new Object[] { mStickSpec, id });
	}
	
	public void writeVertSpec(long id, String descId, String vertSpec, String faceSpec, String normSpec) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		jt.update("insert into StimObjData_vert (id, descId, vertSpec, faceSpec, normSpec) values (?, ?, ?, ?, ?)", 
				new Object[] { id, descId, vertSpec, faceSpec, normSpec});
	}
	
	public void writeVertSpec_update(long id, String vertSpec, String faceSpec, String normSpec) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		jt.update("update StimObjData_vert set vertSpec=?, faceSpec=?, normSpec=? where id=?", 
				new Object[] { vertSpec, faceSpec, normSpec, id });
	}
	
	/**
	 * Get done tasks for the generation.
	 * 
	 * @param genId
	 * @return {@link GenerationTaskDoneList} empty if there is no done tasks
	 *         for the generation in database.
	 */

	public GenerationTaskDoneList readTaskDoneByFullGen(String prefix, long runNum, long genNum) {
		final GenerationTaskDoneList taskDone = new GenerationTaskDoneList();
		taskDone.setGenId(genNum);
		taskDone.setDoneTasks(new ArrayList<TaskDoneEntry>());
		
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		long firstTrial = jt.queryForLong("SELECT firstTrial FROM DescriptiveInfo WHERE prefix = ? "
				+ "AND gaRun = ? AND genNum = ?", new Object[] { prefix, runNum, genNum });
		
		long lastTrial = jt.queryForLong("SELECT firstTrial FROM DescriptiveInfo WHERE prefix = ? "
				+ "AND gaRun = ? AND genNum = ?", new Object[] { prefix, runNum, genNum });
		
		jt.query(
			" select tstamp, task_id, part_done" + 
			" from TaskDone "	+ 
			" where task_id between ? and ?",
			new Object[] { firstTrial, lastTrial },
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					TaskDoneEntry ent = new TaskDoneEntry();
					ent.setTaskId(rs.getLong("task_id")); 
					ent.setTstamp(rs.getLong("tstamp")); 
					ent.setPart_done(rs.getInt("part_done"));
					taskDone.getDoneTasks().add(ent);
				}});
		return taskDone;
	}
	
	public String readPrefixForRunNum(long runNum) {
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);		
		return jt.queryForObject("SELECT prefix FROM DescriptiveInfo WHERE gaRun = ? LIMIT 1",String.class, runNum);
	}
	
	public long readGenIdForRunNum(long runNum) {
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);		
		return jt.queryForLong("SELECT max(genNum) FROM DescriptiveInfo WHERE gaRun = ? ", runNum);
	}
	
	public List<Long> readAllStimIdsForRun(String prefix, long runNum, long genNum) {
		String descId = "^" + prefix + "_r-" + runNum + "_g-" + genNum;
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		
		final List<Long> allIds = new ArrayList<Long>();
		
		jt.query(
				"SELECT id from StimObjData where (descId REGEXP '?')", 
				new Object[] { descId },
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						allIds.add(rs.getLong("task_id"));
					}});
		return allIds;
	}
}