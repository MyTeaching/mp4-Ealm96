package com.example.andrew.androiddevclassday1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements TextWatcher, SeekBar.OnSeekBarChangeListener,
        android.widget.AdapterView.OnItemSelectedListener {

    //set the number formats to be used for the $ amounts , and % amounts
    private static final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance();
    private static final NumberFormat percentFormat =
            NumberFormat.getPercentInstance();
    private static final String TAG = "MainActivity";

    private EditText editTextBillAmount;
    private TextView textViewBillAmount;
    private TextView textView;
    private TextView tipAmount;
    private TextView totalAmount;
    private TextView tip;
    private TextView total;
    private SeekBar sb;
    private Spinner spinner;
    private android.widget.TextView perP;
    private ArrayAdapter<CharSequence> adapter;

    private double billAmount = 0.0;
    private double percent = .15;
    private double tip1;
    private double total1;
    private double numOfP = 1;
    private double roundNum = 1;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.andrew.androiddevclassday1.R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        spinner = (Spinner) findViewById(R.id.TipSpinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.People, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinner != null) {
            spinner.setOnItemSelectedListener(this);
            spinner.setAdapter(adapter);
        }
        //add Listeners to Widgets
        editTextBillAmount = (android.widget.EditText) findViewById(com.example.andrew.androiddevclassday1.R.id.editText_BillAmount);//uncomment this line
        editTextBillAmount.addTextChangedListener((android.text.TextWatcher) this);//uncomment this line
        textViewBillAmount = (android.widget.TextView) findViewById(com.example.andrew.androiddevclassday1.R.id.textView_BillAmount);

        textView = (android.widget.TextView) findViewById(com.example.andrew.androiddevclassday1.R.id.textView);
        tipAmount = (android.widget.TextView) findViewById(com.example.andrew.androiddevclassday1.R.id.TipAmount);
        perP = (android.widget.TextView) findViewById(com.example.andrew.androiddevclassday1.R.id.textView2);

        totalAmount = findViewById(com.example.andrew.androiddevclassday1.R.id.TotalAmount);
        tip = findViewById(com.example.andrew.androiddevclassday1.R.id.Tip);
        total = findViewById(com.example.andrew.androiddevclassday1.R.id.Total);
        sb = findViewById(com.example.andrew.androiddevclassday1.R.id.seekBar);
        sb.setOnSeekBarChangeListener((android.widget.SeekBar.OnSeekBarChangeListener) this);
        textView.setText(percentFormat.format(percent));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.Share) {
            Log.d(TAG, "action settings selected");
            android.content.Intent intent = new android.content.Intent();
            intent.setAction(android.content.Intent.ACTION_SEND);
            intent.putExtra(android.content.Intent.EXTRA_TEXT, "The Bill Amount " +
                    textViewBillAmount.getText() + " Tip " + tipAmount.getText() +
                    " Total Price " + currencyFormat.format(billAmount) + " Split by " + (int) numOfP);
            intent.setType("text/plain");
            android.content.Intent chooser = android.content.Intent.createChooser(intent, "Share with");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
            return true;
        } else if (id == R.id.Info) {
            Log.d(TAG, "action alert selected");
            android.support.v7.app.AlertDialog.Builder alertDialog =
                    new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Message");
            alertDialog.setMessage("The dropdown is used to split the total among friends and the total amount is what each individual must pay.");
            alertDialog.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        try {
            calculate();
        } catch (Exception e) {
            Log.d("MainActivity", "something went wrong, or tip amount equals 0.");
            Log.d("MainActivity", e.getMessage());
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        try {
            Log.d("MainActivity", "inside onTextChanged method: charSequence= " + charSequence);
            //surround risky calculations with try catch (what if billAmount is 0 ?
            //charSequence is converted to a String and parsed to a double for you
            billAmount = Double.parseDouble(charSequence.toString()) * .01;
            Log.d("MainActivity", "Bill Amount = " + billAmount);
            //setText on the textView
            textViewBillAmount.setText(currencyFormat.format(billAmount));
            //perform tip and total calculation and update UI by calling calculate
            calculate();//uncomment this line
        } catch (Exception e) {
            textViewBillAmount.setText(currencyFormat.format(0));
            totalAmount.setText(currencyFormat.format(0));
            tipAmount.setText(currencyFormat.format(0));
            billAmount = 0;
            Log.d("MainActivity", "something went wrong, or tip amount equals 0.");
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

        try {
            calculate();
        } catch (Exception e) {
            Log.d("MainActivity", "something went wrong, or tip amount equals 0.");
            Log.d("MainActivity", e.getMessage());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        percent = progress * .01; //calculate percent based on seeker value
        textView.setText(percentFormat.format(progress * .01));
        calculate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        percent = seekBar.getProgress() * .01;
        calculate();
    }

    // calculate and display tip and total amounts
    private void calculate() {
        try {
            if (roundNum == 1) {
                Log.d("MainActivity", "inside calculate method");

                // calculate the tip and total
                tip1 = billAmount * percent;
                //use the tip example to do the same for the Total

                // display tip and total formatted as currency
                //user currencyFormat instead of percentFormat to set the textViewTip
                tipAmount.setText(currencyFormat.format(tip1));
                //use the tip example to do the same for the Total
                total1 = (billAmount + tip1);
                double per = total1 / numOfP;

                totalAmount.setText(currencyFormat.format(total1));
                perP.setText("Per Person: " + currencyFormat.format(per));
            } else if (roundNum == 2) {
                Log.d("MainActivity", "inside calculate method");

                // calculate the tip and total
                tip1 = billAmount * percent;
                double r = Math.ceil(tip1);
                //use the tip example to do the same for the Total

                // display tip and total formatted as currency
                //user currencyFormat instead of percentFormat to set the textViewTip
                tipAmount.setText(currencyFormat.format(r));
                //use the tip example to do the same for the Total
                total1 = (billAmount + tip1);
                double per = total1 / numOfP;

                totalAmount.setText(currencyFormat.format(total1));
                perP.setText("Per Person: " + currencyFormat.format(per));
            } else if (roundNum == 3) {
                Log.d("MainActivity", "inside calculate method");

                // calculate the tip and total
                tip1 = billAmount * percent;
                //use the tip example to do the same for the Total

                // display tip and total formatted as currency
                //user currencyFormat instead of percentFormat to set the textViewTip
                tipAmount.setText(currencyFormat.format(tip1));
                //use the tip example to do the same for the Total
                total1 = (billAmount + tip1);
                double per = total1 / numOfP;
                double r2D2 = Math.ceil(per);

                totalAmount.setText(currencyFormat.format(total1));
                perP.setText("Per Person: " + currencyFormat.format(r2D2));
            }
        } catch (Exception e) {
            android.util.Log.d("MainActivity", "calculate: Cannot divide by 0");
        }

    }

    @Override
    public void onItemSelected(android.widget.AdapterView<?> adapterView, android.view.View view, int pos, long id) {

        switch (pos) {

            case 1:
                numOfP = 2;
                calculate();
                break;
            case 2:
                numOfP = 3;
                calculate();
                break;
            case 3:
                numOfP = 4;
                calculate();
                break;
            case 4:
                numOfP = 5;
                calculate();
                break;
            default:
                numOfP = 1;
                calculate();
                break;
        }
    }

    @Override
    public void onNothingSelected(android.widget.AdapterView<?> adapterView) {

    }

    public void onRadioClicked(android.view.View view) {
        switch (view.getId()) {
            case com.example.andrew.androiddevclassday1.R.id.radioButton:
                roundNum = 1;
                break;
            case com.example.andrew.androiddevclassday1.R.id.radioButton2:
                roundNum = 2;
                break;
            case com.example.andrew.androiddevclassday1.R.id.radioButton3:
                roundNum = 3;
                break;
            default:
                roundNum = 1;
                break;
        }
    }
}

