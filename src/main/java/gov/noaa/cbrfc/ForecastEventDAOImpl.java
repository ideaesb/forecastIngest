package gov.noaa.cbrfc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class ForecastEventDAOImpl implements ForecastEventDAO {

	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		        this.dataSource = dataSource;
    }
	
	public void insert(ForecastEvent event) {
	String sql = "INSERT INTO forecast_event " +
			     "(simulation_id, ensemble_member, location_id, flow_volumes[]) VALUES (?, ?, ?, ?)";
	Connection conn = null;

	try {
		conn = dataSource.getConnection();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, 1);
		ps.setInt(2, event.getEnsemble().getMember());
		ps.setString(3, event.getLocation().getId());
		//ps.setArray(4, event.getFlowVolumes()); -- needs to java.sql.Array, not double []
		ps.executeUpdate();
		ps.close();

	} catch (SQLException e) {
		throw new RuntimeException(e);

	} finally {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {}
		}
	}

   }

   public ForecastEvent findById(int id){
	   
		String sql = "SELECT * FROM forecast_event WHERE ID = ?";

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ForecastEvent event = null;
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				event = new ForecastEvent();
				event.setEnsemble(new Ensemble());
			}
			rs.close();
			ps.close();
			return event;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
	}


}
