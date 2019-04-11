package com.byagowi.persiancalendar.ui.accounting.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;

import java.util.ArrayList;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DatabaseHandler {
    private Context context;
    private SQLiteDatabase db;
    private DatabaseOpenHelper openHelper;

    public DatabaseHandler(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
        this.context = context;
    }

    public void open() {
        this.db = this.openHelper.getWritableDatabase();
    }

    public void close() {
        this.openHelper.close();
    }

    public String display(int row, int col) {
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, null, null, null, null, null);
        cu.moveToPosition(row);
        return cu.getString(col);
    }

    public int getCostIds(int row) {
        String[] costParam = new String[]{"1"};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Params", null, stringBuilder.append("pStat").append(" = ?").toString(), costParam, null, null, null);
        cu.moveToPosition(row);
        return cu.getInt(0);
    }

    public ArrayList<Integer> getPrices(String date) {
        String[] dateCon = new String[]{date};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        String stringBuilder2 = stringBuilder.append("aDate").append("=?").toString();
        StringBuilder stringBuilder3 = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        Cursor cu_a = sQLiteDatabase.query("Table_Account", null, stringBuilder2, dateCon, null, null, stringBuilder3.append("aParamID").append(" ASC").toString());
        int paramsCount = countParamRows();
        ArrayList<Integer> prices = new ArrayList();
        int i;
        if (cu_a.getCount() != 0) {
            cu_a.moveToFirst();
            for (i = 0; i < paramsCount; i++) {
                if (cu_a.getInt(1) == i + 1) {
                    prices.add(Integer.valueOf(cu_a.getInt(2)));
                    if (!cu_a.isLast()) {
                        cu_a.moveToNext();
                    }
                } else {
                    prices.add(Integer.valueOf(0));
                }
            }
        } else {
            for (i = 0; i < paramsCount; i++) {
                prices.add(Integer.valueOf(0));
            }
        }
        return prices;
    }

    public ArrayList<Integer> getPricesParamID(String date) {
        String[] dateCon = new String[]{date};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        String stringBuilder2 = stringBuilder.append("aDate").append("=?").toString();
        StringBuilder stringBuilder3 = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        Cursor cu_a = sQLiteDatabase.query("Table_Account", null, stringBuilder2, dateCon, null, null, stringBuilder3.append("aParamID").append(" ASC").toString());
        int paramsCount = countParamRows();
        ArrayList<Integer> paramIDs = new ArrayList();
        if (cu_a.getCount() != 0) {
            cu_a.moveToFirst();
            for (int i = 0; i < paramsCount; i++) {
                if (cu_a.getInt(1) == i + 1) {
                    paramIDs.add(Integer.valueOf(cu_a.getInt(1)));
                    if (!cu_a.isLast()) {
                        cu_a.moveToNext();
                    }
                }
            }
        }
        return paramIDs;
    }

    public int getPricesNum(String date) {
        String[] dateCon = new String[]{date};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        String stringBuilder2 = stringBuilder.append("aDate").append("=?").toString();
        StringBuilder stringBuilder3 = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        Cursor cu_a = sQLiteDatabase.query("Table_Account", null, stringBuilder2, dateCon, null, null, stringBuilder3.append("aParamID").append(" ASC").toString());
        int paramsCount = countParamRows();
        ArrayList<Integer> prices = new ArrayList();
        if (cu_a != null) {
            cu_a.moveToFirst();
            for (int i = 0; i < paramsCount; i++) {
                if (cu_a.getInt(1) == i + 1) {
                    prices.add(Integer.valueOf(cu_a.getInt(2)));
                    if (!cu_a.isLast()) {
                        cu_a.moveToNext();
                    }
                } else {
                    prices.add(Integer.valueOf(0));
                }
            }
        }
        return prices.size();
    }

    public String displayCostParams(int row) {
        String[] costParam = new String[]{"1"};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Params", null, stringBuilder.append("pStat").append(" = ?").toString(), costParam, null, null, null);
        cu.moveToPosition(row);
        return cu.getString(1);
    }

    public int getIncomeIds(int row) {
        String[] costParam = new String[]{"0"};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Params", null, stringBuilder.append("pStat").append(" = ?").toString(), costParam, null, null, null);
        cu.moveToPosition(row);
        return cu.getInt(0);
    }

    public String displayIncomeParams(int row) {
        String[] costParam = new String[]{"0"};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Params", null, stringBuilder.append("pStat").append(" = ?").toString(), costParam, null, null, null);
        cu.moveToPosition(row);
        return cu.getString(1);
    }

    public void insert(String name, String phone, String serial) {
        ContentValues cv = new ContentValues();
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        cv.put("aPrice", name);
        databaseOpenHelper = this.openHelper;
        cv.put("aStat", phone);
        databaseOpenHelper = this.openHelper;
        cv.put("aDate", serial);
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        sQLiteDatabase.insert("Table_Account", "aID", cv);
    }

    public void insertCost(int paramId, int price, String date) {
        String[] condition = new String[]{Integer.toString(paramId), date};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aParamID").append(" =?").append(" AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" =? ").toString(), condition, null, null, null);
        if (cu.getCount() == 0) {
            ContentValues cv = new ContentValues();
            DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
            cv.put("aParamID", Integer.valueOf(paramId));
            databaseOpenHelper3 = this.openHelper;
            cv.put("aPrice", Integer.valueOf(price));
            databaseOpenHelper3 = this.openHelper;
            cv.put("aDate", date);
            databaseOpenHelper3 = this.openHelper;
            cv.put("aStat", Integer.valueOf(1));
            sQLiteDatabase = this.db;
            databaseOpenHelper = this.openHelper;
            DatabaseOpenHelper databaseOpenHelper4 = this.openHelper;
            sQLiteDatabase.insert("Table_Account", "aID", cv);
            return;
        }
        cu.moveToFirst();
        int oldPrice = cu.getInt(2);
        ContentValues cv1 = new ContentValues();
        databaseOpenHelper = this.openHelper;
        cv1.put("aPrice", Integer.valueOf(oldPrice + price));
        sQLiteDatabase = this.db;
        databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder2 = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper5 = this.openHelper;
        stringBuilder2 = stringBuilder2.append("aParamID").append(" =?").append(" AND ");
        databaseOpenHelper5 = this.openHelper;
        sQLiteDatabase.update("Table_Account", cv1, stringBuilder2.append("aDate").append(" =?").toString(), condition);
    }

    public void editCost(int paramId, int price, String date) {
        String[] condition = new String[]{Integer.toString(paramId), date};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aParamID").append(" =?").append(" AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" =? ").toString(), condition, null, null, null);
        if (cu.getCount() == 0) {
            Toast.makeText(this.context, "این قسمت برای ویرایش اطلاعات می باشد. اطلاعاتی جهت ویرایش وجود ندارد", Toast.LENGTH_SHORT).show();
            return;
        }
        cu.moveToFirst();
        int oldPrice = cu.getInt(2);
        ContentValues cv1 = new ContentValues();
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        cv1.put("aPrice", Integer.valueOf(oldPrice - price));
        sQLiteDatabase = this.db;
        databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder2 = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper4 = this.openHelper;
        stringBuilder2 = stringBuilder2.append("aParamID").append(" =?").append(" AND ");
        databaseOpenHelper4 = this.openHelper;
        sQLiteDatabase.update("Table_Account", cv1, stringBuilder2.append("aDate").append(" =?").toString(), condition);
    }

    public void insertIncome(int paramId, int price, String date) {
        String[] condition = new String[]{Integer.toString(paramId), date};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aParamID").append(" =?").append(" AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" =? ").toString(), condition, null, null, null);
        if (cu.getCount() == 0) {
            ContentValues cv = new ContentValues();
            DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
            cv.put("aParamID", Integer.valueOf(paramId));
            databaseOpenHelper3 = this.openHelper;
            cv.put("aPrice", Integer.valueOf(price));
            databaseOpenHelper3 = this.openHelper;
            cv.put("aDate", date);
            databaseOpenHelper3 = this.openHelper;
            cv.put("aStat", Integer.valueOf(0));
            sQLiteDatabase = this.db;
            databaseOpenHelper = this.openHelper;
            DatabaseOpenHelper databaseOpenHelper4 = this.openHelper;
            sQLiteDatabase.insert("Table_Account", "aID", cv);
            return;
        }
        cu.moveToFirst();
        int oldPrice = cu.getInt(2);
        ContentValues cv1 = new ContentValues();
        databaseOpenHelper = this.openHelper;
        cv1.put("aPrice", Integer.valueOf(oldPrice + price));
        sQLiteDatabase = this.db;
        databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder2 = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper5 = this.openHelper;
        stringBuilder2 = stringBuilder2.append("aParamID").append(" =?").append(" AND ");
        databaseOpenHelper5 = this.openHelper;
        sQLiteDatabase.update("Table_Account", cv1, stringBuilder2.append("aDate").append(" =?").toString(), condition);
    }

    public void editIncome(int paramId, int price, String date) {
        String[] condition = new String[]{Integer.toString(paramId), date};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aParamID").append(" =?").append(" AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" =? ").toString(), condition, null, null, null);
        if (cu.getCount() == 0) {
            Toast.makeText(this.context, "پیش از ویرایش اطلاعات، مقداری را وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }
        cu.moveToFirst();
        int oldPrice = cu.getInt(2);
        ContentValues cv1 = new ContentValues();
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        cv1.put("aPrice", Integer.valueOf(oldPrice - price));
        sQLiteDatabase = this.db;
        databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder2 = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper4 = this.openHelper;
        stringBuilder2 = stringBuilder2.append("aParamID").append(" =?").append(" AND ");
        databaseOpenHelper4 = this.openHelper;
        sQLiteDatabase.update("Table_Account", cv1, stringBuilder2.append("aDate").append(" =?").toString(), condition);
    }

    public int countRows() {
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        return sQLiteDatabase.query("Table_Account", null, null, null, null, null, null).getCount();
    }

    public int countParamRows() {
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        return sQLiteDatabase.query("Table_Params", null, null, null, null, null, null).getCount();
    }

    public int countCostParams() {
        String[] costParam = new String[]{"1"};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        return sQLiteDatabase.query("Table_Params", null, stringBuilder.append("pStat").append(" = ?").toString(), costParam, null, null, null).getCount();
    }

    public int countIncomeParams() {
        String[] incomeParam = new String[]{"0"};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        return sQLiteDatabase.query("Table_Params", null, stringBuilder.append("pStat").append(" = ?").toString(), incomeParam, null, null, null).getCount();
    }

    public boolean findParam(String param) {
        String[] paramS = new String[]{param};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        if (sQLiteDatabase.query("Table_Params", null, stringBuilder.append("pName").append(" = ?").toString(), paramS, null, null, null).getCount() == 0) {
            return false;
        }
        return true;
    }

    public void insertCostCaption(String caption) {
        ContentValues cv = new ContentValues();
        if (findParam(caption)) {
            Toast.makeText(this.context, "این عنوان قبلا ثبت شده", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        cv.put("pName", caption);
        databaseOpenHelper = this.openHelper;
        cv.put("pStat", Integer.valueOf(1));
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        sQLiteDatabase.insert("Table_Params", "pID", cv);
        Toast.makeText(this.context, "عنوان مورد نظر شما ثبت گردید", Toast.LENGTH_SHORT).show();
    }

    public void insertIncomeCaption(String caption) {
        ContentValues cv = new ContentValues();
        if (findParam(caption)) {
            Toast.makeText(this.context, "این عنوان قبلا ثبت شده", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        cv.put("pName", caption);
        databaseOpenHelper = this.openHelper;
        cv.put("pStat", Integer.valueOf(0));
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        sQLiteDatabase.insert("Table_Params", "pID", cv);
        Toast.makeText(this.context, "عنوان مورد نظر شما ثبت گردید", Toast.LENGTH_SHORT).show();
    }

    public String costOrIncome(int paramID) {
        String[] id = new String[]{Integer.toString(paramID)};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Params", null, stringBuilder.append("pID").append(" = ?").toString(), id, null, null, null);
        cu.moveToPosition(0);
        int stat = cu.getInt(2);
        if (stat == 0) {
            return "Income";
        }
        if (stat == 1) {
            return "Cost";
        }
        return "wrong format";
    }

    public String getMonthReport(String date) {
        String[] dateCon = new String[1];
        char[] dateArray = date.toCharArray();
        String newDate = "";
        for (int j = 3; j < dateArray.length; j++) {
            newDate = newDate + dateArray[j];
        }
        dateCon[0] = "%" + newDate;
        String params = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" LIKE ?").toString(), dateCon, null, null, null);
        if (cu.getCount() > 0) {
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                params = params + Integer.toString(cu.getInt(1)) + " : " + Integer.toString(cu.getInt(2)) + ",   ";
            }
        } else {
            Toast.makeText(this.context, "برای این ماه هنوز گزارشی ثبت نشده است", Toast.LENGTH_SHORT).show();
        }
        return params;
    }

    public int calculateRangeIncome(String dateStart, String dateEnd) {
        int totalIncome = 0;
        String[] datesCon = new String[]{dateStart, dateEnd};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aDate").append(" >= ? AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" <= ? ").toString(), datesCon, null, null, null);
        if (cu.getCount() > 0) {
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 0) {
                    totalIncome += cu.getInt(2);
                }
            }
        }
        return totalIncome;
    }

    public int calculateRangeCost(String dateStart, String dateEnd) {
        int totalCost = 0;
        String[] datesCon = new String[]{dateStart, dateEnd};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aDate").append(" >= ? AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" <= ? ").toString(), datesCon, null, null, null);
        if (cu.getCount() > 0) {
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 1) {
                    totalCost += cu.getInt(2);
                }
            }
        }
        return totalCost;
    }

    public int calculateMonthCost(String date) {
        String[] dateCon = new String[1];
        char[] dateArr = date.toCharArray();
        String newDate = "";
        for (int j = 3; j < dateArr.length; j++) {
            if (j == 3) {
                newDate = Character.toString(dateArr[j]);
            } else {
                newDate = newDate + dateArr[j];
            }
        }
        dateCon[0] = "%" + newDate;
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" LIKE ?").toString(), dateCon, null, null, null);
        int totalCost = 0;
        if (cu.getCount() > 0) {
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 1) {
                    totalCost += cu.getInt(2);
                }
            }
        }
        return totalCost;
    }

    public int calculateDailyCost(String date) {
        String[] dateCon = new String[]{date};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" = ?").toString(), dateCon, null, null, null);
        int totalCost = 0;
        if (cu.getCount() > 0) {
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 1) {
                    totalCost += cu.getInt(2);
                }
            }
        }
        return totalCost;
    }

    public int calculateMonthIncome(String date) {
        String[] dateCon = new String[1];
        char[] dateArr = date.toCharArray();
        String newDate = "";
        for (int j = 3; j < dateArr.length; j++) {
            if (j == 3) {
                newDate = Character.toString(dateArr[j]);
            } else {
                newDate = newDate + dateArr[j];
            }
        }
        dateCon[0] = "%" + newDate;
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" LIKE ?").toString(), dateCon, null, null, null);
        int totalIncome = 0;
        if (cu.getCount() > 0) {
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 0) {
                    totalIncome += cu.getInt(2);
                }
            }
        }
        return totalIncome;
    }

    public int calculateDailyIncome(String date) {
        String[] dateCon = new String[]{date};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" = ?").toString(), dateCon, null, null, null);
        int totalIncome = 0;
        if (cu.getCount() > 0) {
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 0) {
                    totalIncome += cu.getInt(2);
                }
            }
        }
        return totalIncome;
    }

    public ArrayList<String> DetailsCostSpecific(String dateStart, String dateEnd) {
        ArrayList<String> resultCost = new ArrayList();
        String[] datesCon = new String[]{dateStart, dateEnd};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aDate").append(" >= ? AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" <= ? ").toString(), datesCon, null, null, null);
        SQLiteDatabase sQLiteDatabase2 = this.db;
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        Cursor cuP = sQLiteDatabase2.query("Table_Params", null, null, null, null, null, null);
        if (cu.getCount() > 0) {
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 1) {
                    for (int j = 0; j < cuP.getCount(); j++) {
                        cuP.moveToPosition(j);
                        if (cuP.getInt(0) == cu.getInt(1)) {
                            resultCost.add(cuP.getString(1));
                        }
                    }
                    resultCost.add(Integer.toString(cu.getInt(2)));
                }
            }
        }
        return resultCost;
    }

    public ArrayList<String> DetailsIncomeSpecific(String dateStart, String dateEnd) {
        ArrayList<String> resultCost = new ArrayList();
        String[] datesCon = new String[]{dateStart, dateEnd};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aDate").append(" >= ? AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" <= ? ").toString(), datesCon, null, null, null);
        SQLiteDatabase sQLiteDatabase2 = this.db;
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        Cursor cuP = sQLiteDatabase2.query("Table_Params", null, null, null, null, null, null);
        if (cu.getCount() > 0) {
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 0) {
                    for (int j = 0; j < cuP.getCount(); j++) {
                        cuP.moveToPosition(j);
                        if (cuP.getInt(0) == cu.getInt(1)) {
                            resultCost.add(cuP.getString(1));
                        }
                    }
                    resultCost.add(Integer.toString(cu.getInt(2)));
                }
            }
        }
        return resultCost;
    }

    public ArrayList<String> DetailsCost(String dateStart, String dateEnd) {
        ArrayList<String> resultCost = new ArrayList();
        ArrayList<Integer> duplicateParams = new ArrayList();
        String[] datesCon = new String[]{dateStart, dateEnd};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aDate").append(" >= ? AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" <= ? ").toString(), datesCon, null, null, null);
        SQLiteDatabase sQLiteDatabase2 = this.db;
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        Cursor cuP = sQLiteDatabase2.query("Table_Params", null, null, null, null, null, null);
        if (cu.getCount() > 0) {
            int k;
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 1) {
                    boolean paramExistFlag = false;
                    for (k = 0; k < duplicateParams.size(); k += 2) {
                        if (cu.getInt(1) == ((Integer) duplicateParams.get(k)).intValue()) {
                            ArrayList<Integer> arrayList = duplicateParams;
                            arrayList.set(k + 1, Integer.valueOf(((Integer) duplicateParams.get(k + 1)).intValue() + cu.getInt(2)));
                            paramExistFlag = true;
                        }
                    }
                    if (!paramExistFlag) {
                        duplicateParams.add(Integer.valueOf(cu.getInt(1)));
                        duplicateParams.add(Integer.valueOf(cu.getInt(2)));
                    }
                }
            }
            for (int j = 0; j < duplicateParams.size(); j += 2) {
                for (k = 0; k < cuP.getCount(); k++) {
                    cuP.moveToPosition(k);
                    if (cuP.getInt(0) == ((Integer) duplicateParams.get(j)).intValue()) {
                        resultCost.add(cuP.getString(1));
                        break;
                    }
                }
                ArrayList<String> arrayList2 = resultCost;
                arrayList2.add(Integer.toString(((Integer) duplicateParams.get(j + 1)).intValue()));
            }
        }
        return resultCost;
    }

    public ArrayList<String> DetailsIncome(String dateStart, String dateEnd) {
        ArrayList<String> resultCost = new ArrayList();
        ArrayList<Integer> duplicateParams = new ArrayList();
        String[] datesCon = new String[]{dateStart, dateEnd};
        SQLiteDatabase sQLiteDatabase = this.db;
        DatabaseOpenHelper databaseOpenHelper = this.openHelper;
        StringBuilder stringBuilder = new StringBuilder();
        DatabaseOpenHelper databaseOpenHelper2 = this.openHelper;
        stringBuilder = stringBuilder.append("aDate").append(" >= ? AND ");
        databaseOpenHelper2 = this.openHelper;
        Cursor cu = sQLiteDatabase.query("Table_Account", null, stringBuilder.append("aDate").append(" <= ? ").toString(), datesCon, null, null, null);
        SQLiteDatabase sQLiteDatabase2 = this.db;
        DatabaseOpenHelper databaseOpenHelper3 = this.openHelper;
        Cursor cuP = sQLiteDatabase2.query("Table_Params", null, null, null, null, null, null);
        if (cu.getCount() > 0) {
            int k;
            for (int i = 0; i < cu.getCount(); i++) {
                cu.moveToPosition(i);
                if (cu.getInt(3) == 0) {
                    boolean paramExistFlag = false;
                    for (k = 0; k < duplicateParams.size(); k += 2) {
                        if (cu.getInt(1) == ((Integer) duplicateParams.get(k)).intValue()) {
                            ArrayList<Integer> arrayList = duplicateParams;
                            arrayList.set(k + 1, Integer.valueOf(((Integer) duplicateParams.get(k + 1)).intValue() + cu.getInt(2)));
                            paramExistFlag = true;
                        }
                    }
                    if (!paramExistFlag) {
                        duplicateParams.add(Integer.valueOf(cu.getInt(1)));
                        duplicateParams.add(Integer.valueOf(cu.getInt(2)));
                    }
                }
            }
            for (int j = 0; j < duplicateParams.size(); j += 2) {
                for (k = 0; k < cuP.getCount(); k++) {
                    cuP.moveToPosition(k);
                    if (cuP.getInt(0) == ((Integer) duplicateParams.get(j)).intValue()) {
                        resultCost.add(cuP.getString(1));
                        break;
                    }
                }
                ArrayList<String> arrayList2 = resultCost;
                arrayList2.add(Integer.toString(((Integer) duplicateParams.get(j + 1)).intValue()));
            }
        }
        return resultCost;
    }

    public String[] getMonthAndYear(String date) {
        char[] dateArr = date.toCharArray();
        String[] monthAndYear = new String[]{Character.toString(dateArr[3]) + Character.toString(dateArr[4]), Character.toString(dateArr[6]) + Character.toString(dateArr[7]) + Character.toString(dateArr[8]) + Character.toString(dateArr[9])};
        String str = monthAndYear[0];
        int i = -1;
        switch (str.hashCode()) {
            case 1537:
                if (str.equals("01")) {
                    i = 0;
                    break;
                }
                break;
            case 1538:
                if (str.equals("02")) {
                    i = 1;
                    break;
                }
                break;
            case 1539:
                if (str.equals("03")) {
                    i = 2;
                    break;
                }
                break;
            case 1540:
                if (str.equals("04")) {
                    i = 3;
                    break;
                }
                break;
            case 1541:
                if (str.equals("05")) {
                    i = 4;
                    break;
                }
                break;
            case 1542:
                if (str.equals("06")) {
                    i = 5;
                    break;
                }
                break;
            case 1543:
                if (str.equals("07")) {
                    i = 6;
                    break;
                }
                break;
            case 1544:
                if (str.equals("08")) {
                    i = 7;
                    break;
                }
                break;
            case 1545:
                if (str.equals("09")) {
                    i = 8;
                    break;
                }
                break;
            case 1567:
                if (str.equals("10")) {
                    i = 9;
                    break;
                }
                break;
            case 1568:
                if (str.equals("11")) {
                    i = 10;
                    break;
                }
                break;
            case 1569:
                if (str.equals("12")) {
                    i = 11;
                    break;
                }
                break;
        }
        switch (i) {
            case 0:
                monthAndYear[0] = "فروردین";
                break;
            case 1:
                monthAndYear[0] = "اردیبهشت";
                break;
            case 2:
                monthAndYear[0] = "خرداد";
                break;
            case 3:
                monthAndYear[0] = "تیر";
                break;
            case 4:
                monthAndYear[0] = "مرداد";
                break;
            case 5:
                monthAndYear[0] = "شهریور";
                break;
            case 6:
                monthAndYear[0] = "مهر";
                break;
            case 7:
                monthAndYear[0] = "آبان";
                break;
            case 8:
                monthAndYear[0] = "آذر";
                break;
            case 9:
                monthAndYear[0] = "دی";
                break;
            case 10:
                monthAndYear[0] = "بهمن";
                break;
            case 11:
                monthAndYear[0] = "اسفند";
                break;
            default:
                monthAndYear[0] = context.getString(R.string.not_valid_a_month);
                break;
        }
        return monthAndYear;
    }
}
