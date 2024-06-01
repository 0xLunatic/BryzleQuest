package lunatic.bryzlequest.db;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.sql.*;

public class DbConnection {
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    public DbConnection(String host, int port, String database, String user, String password) {
        dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
        dbUser = user;
        dbPassword = password;
    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS bryzle_quest ("
                + "playerUuid VARCHAR(36) PRIMARY KEY,"
                + "playerName VARCHAR(255) NOT NULL,"
                + "difficulty VARCHAR(255),"
                + "currentQuest VARCHAR(255),"
                + "itemAmount INT,"
                + "bryzleLevel INT,"
                + "bryzleReputation INT,"
                + "completedQuest INT"
                + ");";
        executeUpdate(sql);
    }

    public void insertPlayer(String playerUuid, String playerName, String difficulty, Material currentQuest, int itemAmount, int bryzleLevel, int bryzleReputation, int completedQuest) {
        String selectSql = "SELECT * FROM bryzle_quest WHERE playerUuid = ?";
        String insertSql = "INSERT INTO bryzle_quest(playerUuid, playerName, difficulty, currentQuest, itemAmount, bryzleLevel, bryzleReputation, completedQuest) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE bryzle_quest SET playerName = ?, difficulty = ?, currentQuest = ?, itemAmount = ?, bryzleLevel = ?, bryzleReputation = ?, completedQuest = ? WHERE playerUuid = ?";

        try (Connection conn = this.connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            // Check if the player already exists
            selectStmt.setString(1, playerUuid);
            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                // Player already exists, perform update
                updateStmt.setString(1, playerName);
                updateStmt.setString(2, difficulty);
                updateStmt.setString(3, currentQuest != null ? currentQuest.name() : null);
                updateStmt.setInt(4, itemAmount);
                updateStmt.setInt(5, bryzleLevel);
                updateStmt.setInt(6, bryzleReputation);
                updateStmt.setInt(7, completedQuest);
                updateStmt.setString(8, playerUuid);
                updateStmt.executeUpdate();
            } else {
                // Player does not exist, perform insert
                insertStmt.setString(1, playerUuid);
                insertStmt.setString(2, playerName);
                insertStmt.setString(3, difficulty);
                insertStmt.setString(4, currentQuest != null ? currentQuest.name() : null);
                insertStmt.setInt(5, itemAmount);
                insertStmt.setInt(6, bryzleLevel);
                insertStmt.setInt(7, bryzleReputation);
                insertStmt.setInt(8, completedQuest);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updatePlayer(String playerUuid, Material currentQuest, int itemAmount, int bryzleLevel, int bryzleReputation, int completedQuest) {
        String sql = "UPDATE bryzle_quest SET currentQuest = ?, itemAmount = ?, bryzleLevel = ?, bryzleReputation = ?, completedQuest = ? WHERE playerUuid = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentQuest != null ? currentQuest.name() : null);
            pstmt.setInt(2, itemAmount);
            pstmt.setInt(3, bryzleLevel);
            pstmt.setInt(4, bryzleReputation);
            pstmt.setInt(5, completedQuest);
            pstmt.setString(6, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deletePlayer(String playerUuid) {
        String sql = "DELETE FROM bryzle_quest WHERE playerUuid = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Object getColumnValue(String playerUuid, String columnName) {
        String sql = "SELECT " + columnName + " FROM bryzle_quest WHERE playerUuid = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getObject(columnName);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Getter for playerName
    public String getPlayerName(String playerUuid) {
        String sql = "SELECT playerName FROM bryzle_quest WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("playerName");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Setter for playerName
    public void setPlayerName(String playerUuid, String playerName) {
        String sql = "UPDATE bryzle_quest SET playerName = ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Adder for itemAmount
    public void addItemAmount(String playerUuid, int amount) {
        String sql = "UPDATE bryzle_quest SET itemAmount = itemAmount + ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Subtractor for itemAmount
    public void subtractItemAmount(String playerUuid, int amount) {
        String sql = "UPDATE bryzle_quest SET itemAmount = itemAmount - ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Getter for currentQuest
    public Material getCurrentQuest(String playerUuid) {
        String sql = "SELECT currentQuest FROM bryzle_quest WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                String questName = resultSet.getString("currentQuest");
                if (questName != null) {
                    return Material.getMaterial(questName);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Setter for currentQuest
    public void setCurrentQuest(String playerUuid, Material currentQuest) {
        String questName = currentQuest != null ? currentQuest.name() : null;
        String sql = "UPDATE bryzle_quest SET currentQuest = ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, questName);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Adder for bryzleLevel
    public void addBryzleLevel(String playerUuid, int amount) {
        String sql = "UPDATE bryzle_quest SET bryzleLevel = bryzleLevel + ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Subtractor for bryzleLevel
    public void subtractBryzleLevel(String playerUuid, int amount) {
        String sql = "UPDATE bryzle_quest SET bryzleLevel = bryzleLevel - ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Adder for bryzleReputation
    public void addBryzleReputation(String playerUuid, int amount) {
        String sql = "UPDATE bryzle_quest SET bryzleReputation = bryzleReputation + ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Subtractor for bryzleReputation
    public void subtractBryzleReputation(String playerUuid, int amount) {
        String sql = "UPDATE bryzle_quest SET bryzleReputation = bryzleReputation - ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Adder for completedQuest
    public void addCompletedQuest(String playerUuid, int amount) {
        String sql = "UPDATE bryzle_quest SET completedQuest = completedQuest + ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Subtractor for completedQuest
    public void subtractCompletedQuest(String playerUuid, int amount) {
        String sql = "UPDATE bryzle_quest SET completedQuest = completedQuest - ? WHERE playerUuid = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Implement similar methods for other columns...

    private void executeUpdate(String sql) {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setColumnValue(String playerUuid, String columnName, Object value) {
        String sql = "UPDATE bryzle_quest SET " + columnName + " = ? WHERE playerUuid = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, value);
            pstmt.setString(2, playerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}