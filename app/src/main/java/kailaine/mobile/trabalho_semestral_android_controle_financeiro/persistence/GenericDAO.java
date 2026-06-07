package kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence;
/*
 *@author:<Kailaine Almeida de Souza RA: 1110482313026>
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GenericDAO extends SQLiteOpenHelper {

    private static final String DATABASE = "railway";
    private static final int DATABASE_VER = 1;

    private static final String CREATE_TABLE_DESPESA = "CREATE TABLE despesa ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "valor REAL NOT NULL, " +
            "data TEXT NOT NULL );";

    private static final String CREATE_TABLE_RECEITA = "CREATE TABLE receita ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "valor REAL NOT NULL, " +
            "data TEXT NOT NULL);";

    private static final String CREATE_TABLE_RESERVA = "CREATE TABLE reserva ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "valor REAL NOT NULL, " +
            "data TEXT NOT NULL,"+
            "metaFinanceira INT, "+
            "FOREIGN KEY (metaFinanceira) REFERENCES metaFinanceira(id));";

    private static final String CREATE_TABLE_METAS = "CREATE TABLE metaFinanceira ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "valor REAL NOT NULL, " +
            "nome TEXT UNIQUE NOT NULL);";

    public GenericDAO(Context context) {
        super(context, DATABASE, null, DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_DESPESA);
        sqLiteDatabase.execSQL(CREATE_TABLE_RECEITA);
        sqLiteDatabase.execSQL(CREATE_TABLE_METAS);
        sqLiteDatabase.execSQL(CREATE_TABLE_RESERVA);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int antigaVersao, int novaVersao) {
        if (novaVersao > antigaVersao) {
            
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS despesa");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS receita");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS metaFinanceira");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS reserva");
            onCreate(sqLiteDatabase);

        }
        
    }

}