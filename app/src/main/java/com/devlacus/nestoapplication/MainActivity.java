package com.devlacus.nestoapplication;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.Manifest;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.AlertDialog;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements MQTTClient.MQTTClientListener, GuestIdAPI.GuestIdInterface {

    Button meet;
    boolean flag;
    VideoView video;

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    private int speechRetryCount = 0;
    String selectedPerson;

    String email;

    Button btnSubmit;

    BottomSheetDialog bottomSheetDialog;

    BottomSheetDialog departmentBottomSheetDialog;

    Button buttonYes;

    Button buttonNo;
    TextInputEditText name;
    TextInputEditText purpose;
    TextInputEditText company;
    TextInputEditText mobile;

    boolean actionflag;

    SpeechRecognizer speechRecognizer;

    boolean bottomSheetFlag;

    boolean dialogFlag;

    AlertDialog yesOrNoDialog;

    private ApiCaller apiCaller;


    AlertDialog emailFormAlert;

    private boolean voiceFlag;

    boolean mqttflag;

    boolean yesNoFlag;

    Button btnYes;

    Button btnNo;

    boolean toggle;

    boolean submitFlag;

    boolean bottomSheet;

    String[] persons;

    int departmentIndex;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private final AtomicBoolean isDetecting = new AtomicBoolean(false);
    private boolean isFaceDetected = false;

    AlertDialog dialogYesOrNo;

    boolean startFlag;

    boolean resetactvity;

    boolean aftersubmitflag;


    private final Handler activityDelayHandler = new Handler();
    private final Runnable activityDelayRunnable = new Runnable() {
        @Override
        public void run() {

            if (!isFaceDetected) {
                meet.setVisibility(View.GONE);
                video.setVisibility(View.GONE);
                isDetecting.set(false);
                if(departmentBottomSheetDialog != null)
                {
                    bottomSheetFlag = false;
                    departmentBottomSheetDialog.dismiss();
                }
                if(bottomSheetDialog != null)
                {
                    bottomSheet = false;
                    bottomSheetDialog.dismiss();
                }
                if(yesOrNoDialog != null)
                    yesOrNoDialog.dismiss();
                if(emailFormAlert != null)
                    emailFormAlert.dismiss();
                if(dialogYesOrNo != null)
                    dialogYesOrNo.dismiss();
                stopSpeechRecognition();
                mqttflag = false;
                emailFlag = false;
                actionflag = false;
                toggle = false;
                submitFlag = false;
                voiceAction = false;
                startFlag = false;
                resetactvity = false;

                aftersubmitflag = false;

                if(mqttClient != null)
                    mqttClient.disconnect();
            }
        }
    };

    String[] departments = {"MD Office", "Office Staff", "HSE", "Maintainance Admin", "HR Office", "Buying Department", "Accounts", "IT"};

    // Step 2: Create Corresponding People Lists
    // Assuming people are segregated into departments

    String blana = "null";
    String[][] departmentPersons = {
            {"Farsana", "Siddique Pallathil - Managing Director", "Jamal KP - Managing Director", "Amina"},
            {"Shamsudheen NP", "Faris", "Sayed", "Alex ninan", "Muhammed Nizar - Head of Cost Control", "Sandeep - Project Manager", "Sareena", "Muhammed Fayiz", "Arshad - Design Manager", "Ishack - Project Engineer", "Haja Shake - MEP Engineer", "Lijo - Mechanical Engineer", "Sanid - Electrical Engineer", "Ashique - Project Engineer", "Shyam OM - Department Head,Graphics", "swadik - Loyalty Executive,Marketing"},
            {"Anvar - Fire&Safety Engineer","vivek"},
            {"Karla - Maintenance Admin","jovian"},
            {"Hameed PP - HR Manager", "Sabith - Payroll Manager,HR", "Salam", "Fajis - HR-Operation"},
            {"Firoz P Mohamad Ali - Group Buying Head-Lifestyle", "Muhammed Ashfaq K.K", "Faisal Abbas", "Ansil.T.Hameed", "Mujeeb Rahman.K", "Shanavas P.K", "Subuhathali.M", "Mohan Kumar Rai", "Tariq - Buying Head-HouseHold", "Thanzil", "Manu Prasad", "Askar pp", "Mansoor - Buying Head,Buying-Electronics & IT", "Yasser VM", "Ashique C", "Sumesh Mohanan", "Anwar", "Arafath", "Yoonus", "MA Jaleel", "Abdul Savad - Buying Manager(food)", "Muhammed MK - Senior Category Manager Grocer Food", "Muhammed Ali Komath - Senior Category Manager Grocer Food", "Saleesh - Buying Manager UK & USA Imports", "Mohan", "Hashim", "Shahad", "Jeremy", "Abdullah", "Sajeer E", "Afsal - Category Manager -Bakery,Butchery &Hot food,Fresh food", "Shahul", "ramsheed - Category Manager Frozen Food,FMCG", "Subhan - Category Manager Chilled&Diary,Fresh food", "Krishna Raj", "Nizar Komath - Category Manager Grocery Non Food", "Risad", "Noushad KT - Category Manager Health & Beauty", "Favas", "Rashid A - Buying Head,Private Label", "Monica Sapkota", "Junaid - Category Manager-Grocer Food-Pvt Label", "Thwelhath KP", "Shakir Mohammed - Buying Manager", "Shafeek KS - Category Manager-Grocery Non Food-Pvt Label", " Siaddin Sidhique - Category Manager-Delicatessen & RTC"},
            {"Rafeek - Finance Manager", "Nisar KP - Senior Accountant", "Salman", "Prathap - Senior Accountant"},
            {"Namshid PP - IT Manager", "Lenish Kannan - IT Head"}
    };

//    String[] emails = {"farsana@devlacus.com", "hameed@devlacus.com", "shamsudeen@devlacus.com", "namshid@devlacus.com", "joviandcunha@devlacus.com"};

  //  String[] employee_id = {"nestogroup-942b-4c08-8d17-02732b96a2b4", "nestogroup-942b-4c08-8d17-02732b96a2b3", "nestogroup-942b-4c08-8d17-02732b96a2b2", "nestogroup-942b-4c08-8d17-02732b96a2b1", "nestogroup-942b-4c08-8d17-02732b96a2b6", "nestogroup-942b-4c08-8d17-02732b96a2b7", "nestogroup-942b-4c08-8d17-02732b96a2b8", "nestogroup-942b-4c08-8d17-02732b96a2b9", "nestogroup-942b-4c08-8d17-02732b96a2b10", "nestogroup-942b-4c08-8d17-02732b96a2b11", "nestogroup-942b-4c08-8d17-02732b96a2b12", "nestogroup-942b-4c08-8d17-02732b96a2b13", "nestogroup-942b-4c08-8d17-02732b96a2b14", "nestogroup-942b-4c08-8d17-02732b96a2b15", "nestogroup-942b-4c08-8d17-02732b96a2b16", "nestogroup-942b-4c08-8d17-02732b96a2b17", "nestogroup-942b-4c08-8d17-02732b96a2b18", "nestogroup-942b-4c08-8d17-02732b96a2b19", "nestogroup-942b-4c08-8d17-02732b96a2b20", "nestogroup-942b-4c08-8d17-02732b96a2b21", "nestogroup-942b-4c08-8d17-02732b96a2b22", "nestogroup-942b-4c08-8d17-02732b96a2b23", "nestogroup-942b-4c08-8d17-02732b96a2b24", "nestogroup-942b-4c08-8d17-02732b96a2b25", "nestogroup-942b-4c08-8d17-02732b96a2b26", "nestogroup-942b-4c08-8d17-02732b96a2b27", "nestogroup-942b-4c08-8d17-02732b96a2b28", "nestogroup-942b-4c08-8d17-02732b96a2b29", "nestogroup-942b-4c08-8d17-02732b96a2b30", "nestogroup-942b-4c08-8d17-02732b96a2b31", "nestogroup-942b-4c08-8d17-02732b96a2b32", "nestogroup-942b-4c08-8d17-02732b96a2b33", "nestogroup-942b-4c08-8d17-02732b96a2b34", "nestogroup-942b-4c08-8d17-02732b96a2b35", "nestogroup-942b-4c08-8d17-02732b96a2b36", "nestogroup-942b-4c08-8d17-02732b96a2b37", "nestogroup-942b-4c08-8d17-02732b96a2b38", "nestogroup-942b-4c08-8d17-02732b96a2b39", "nestogroup-942b-4c08-8d17-02732b96a2b40", "nestogroup-942b-4c08-8d17-02732b96a2b41", "nestogroup-942b-4c08-8d17-02732b96a2b42", "nestogroup-942b-4c08-8d17-02732b96a2b43", "nestogroup-942b-4c08-8d17-02732b96a2b44", "nestogroup-942b-4c08-8d17-02732b96a2b45", "nestogroup-942b-4c08-8d17-02732b96a2b46", "nestogroup-942b-4c08-8d17-02732b96a2b47", "nestogroup-942b-4c08-8d17-02732b96a2b48", "nestogroup-942b-4c08-8d17-02732b96a2b49", "nestogroup-942b-4c08-8d17-02732b96a2b50", "nestogroup-942b-4c08-8d17-02732b96a2b51", "nestogroup-942b-4c08-8d17-02732b96a2b52", "nestogroup-942b-4c08-8d17-02732b96a2b53", "nestogroup-942b-4c08-8d17-02732b96a2b54", "nestogroup-942b-4c08-8d17-02732b96a2b55", "nestogroup-942b-4c08-8d17-02732b96a2b56", "nestogroup-942b-4c08-8d17-02732b96a2b57", "nestogroup-942b-4c08-8d17-02732b96a2b58", "nestogroup-942b-4c08-8d17-02732b96a2b59", "nestogroup-942b-4c08-8d17-02732b96a2b60", "nestogroup-942b-4c08-8d17-02732b96a2b61", "nestogroup-942b-4c08-8d17-02732b96a2b62", "nestogroup-942b-4c08-8d17-02732b96a2b63", "nestogroup-942b-4c08-8d17-02732b96a2b64", "nestogroup-942b-4c08-8d17-02732b96a2b65", "nestogroup-942b-4c08-8d17-02732b96a2b66", "nestogroup-942b-4c08-8d17-02732b96a2b67", "nestogroup-942b-4c08-8d17-02732b96a2b68", "nestogroup-942b-4c08-8d17-02732b96a2b69", "nestogroup-942b-4c08-8d17-02732b96a2b70", "nestogroup-942b-4c08-8d17-02732b96a2b71", "nestogroup-942b-4c08-8d17-02732b96a2b72", "nestogroup-942b-4c08-8d17-02732b96a2b73", "nestogroup-942b-4c08-8d17-02732b96a2b74", "nestogroup-942b-4c08-8d17-02732b96a2b75", "nestogroup-942b-4c08-8d17-02732b96a2b76", "nestogroup-942b-4c08-8d17-02732b96a2b77", "nestogroup-942b-4c08-8d17-02732b96a2b78", "nestogroup-942b-4c08-8d17-02732b96a2b79", "nestogroup-942b-4c08-8d17-02732b96a2b80", "nestogroup-942b-4c08-8d17-02732b96a2b81"};

    String[][] employee_id = {
            {"nestogroup-942b-4c08-8d17-02732b96a2b4", "nestogroup-942b-4c08-8d17-02732b96a2b6", "nestogroup-942b-4c08-8d17-02732b96a2b7", "nestogroup-942b-4c08-8d17-02732b96a2b8"},
            {"nestogroup-942b-4c08-8d17-02732b96a2b2", "nestogroup-942b-4c08-8d17-02732b96a2b9", "nestogroup-942b-4c08-8d17-02732b96a2b10", "nestogroup-942b-4c08-8d17-02732b96a2b11", "nestogroup-942b-4c08-8d17-02732b96a2b12", "nestogroup-942b-4c08-8d17-02732b96a2b13", "nestogroup-942b-4c08-8d17-02732b96a2b14", "nestogroup-942b-4c08-8d17-02732b96a2b15", "nestogroup-942b-4c08-8d17-02732b96a2b16", "nestogroup-942b-4c08-8d17-02732b96a2b17", "nestogroup-942b-4c08-8d17-02732b96a2b18", "nestogroup-942b-4c08-8d17-02732b96a2b19", "nestogroup-942b-4c08-8d17-02732b96a2b20", "nestogroup-942b-4c08-8d17-02732b96a2b21", "nestogroup-942b-4c08-8d17-02732b96a2b80", "nestogroup-942b-4c08-8d17-02732b96a281"},
            {"nestogroup-942b-4c08-8d17-02732b96a2b22","261daf56-0287-43fe-9c13-93f295a3c371"},
            {"nestogroup-942b-4c08-8d17-02732b96a2b23","bbac478c-ff0f-40db-b285-35c8ac8c38ae"},
            {"nestogroup-942b-4c08-8d17-02732b96a2b3", "nestogroup-942b-4c08-8d17-02732b96a2b24", "nestogroup-942b-4c08-8d17-02732b96a2b25", "nestogroup-942b-4c08-8d17-02732b96a2b26"},
            {"nestogroup-942b-4c08-8d17-02732b96a2b27", "nestogroup-942b-4c08-8d17-02732b96a2b28", "nestogroup-942b-4c08-8d17-02732b96a2b29", "nestogroup-942b-4c08-8d17-02732b96a2b30", "nestogroup-942b-4c08-8d17-02732b96a2b31", "nestogroup-942b-4c08-8d17-02732b96a2b32", "nestogroup-942b-4c08-8d17-02732b96a2b33", "nestogroup-942b-4c08-8d17-02732b96a2b34", "nestogroup-942b-4c08-8d17-02732b96a2b35", "nestogroup-942b-4c08-8d17-02732b96a2b36", "nestogroup-942b-4c08-8d17-02732b96a2b37", "nestogroup-942b-4c08-8d17-02732b96a2b38", "nestogroup-942b-4c08-8d17-02732b96a2b39", "nestogroup-942b-4c08-8d17-02732b96a2b40", "nestogroup-942b-4c08-8d17-02732b96a2b41", "nestogroup-942b-4c08-8d17-02732b96a2b42", "nestogroup-942b-4c08-8d17-02732b96a2b43", "nestogroup-942b-4c08-8d17-02732b96a2b44", "nestogroup-942b-4c08-8d17-02732b96a2b45", "nestogroup-942b-4c08-8d17-02732b96a2b46", "nestogroup-942b-4c08-8d17-02732b96a2b47", "nestogroup-942b-4c08-8d17-02732b96a2b48", "nestogroup-942b-4c08-8d17-02732b96a2b49", "nestogroup-942b-4c08-8d17-02732b96a2b50", "nestogroup-942b-4c08-8d17-02732b96a2b51", "nestogroup-942b-4c08-8d17-02732b96a2b52", "nestogroup-942b-4c08-8d17-02732b96a2b53", "nestogroup-942b-4c08-8d17-02732b96a2b54", "nestogroup-942b-4c08-8d17-02732b96a2b55", "nestogroup-942b-4c08-8d17-02732b96a2b56", "nestogroup-942b-4c08-8d17-02732b96a2b57", "nestogroup-942b-4c08-8d17-02732b96a2b58", "nestogroup-942b-4c08-8d17-02732b96a2b59", "nestogroup-942b-4c08-8d17-02732b96a2b60", "nestogroup-942b-4c08-8d17-02732b96a2b61", "nestogroup-942b-4c08-8d17-02732b96a2b62", "nestogroup-942b-4c08-8d17-02732b96a2b63", "nestogroup-942b-4c08-8d17-02732b96a2b64", "nestogroup-942b-4c08-8d17-02732b96a2b65", "nestogroup-942b-4c08-8d17-02732b96a2b66", "nestogroup-942b-4c08-8d17-02732b96a2b67", "nestogroup-942b-4c08-8d17-02732b96a2b68", "nestogroup-942b-4c08-8d17-02732b96a2b69", "nestogroup-942b-4c08-8d17-02732b96a2b70", "nestogroup-942b-4c08-8d17-02732b96a2b71", "nestogroup-942b-4c08-8d17-02732b96a2b72", "nestogroup-942b-4c08-8d17-02732b96a2b73", "nestogroup-942b-4c08-8d17-02732b96a2b74"},
            {"nestogroup-942b-4c08-8d17-02732b96a2b75", "nestogroup-942b-4c08-8d17-02732b96a2b76", "nestogroup-942b-4c08-8d17-02732b96a2b77", "nestogroup-942b-4c08-8d17-02732b96a2b78"},
            {"nestogroup-942b-4c08-8d17-02732b96a2b1", "nestogroup-942b-4c08-8d17-02732b96a2b79"}
    };



    String emp_id;

    String base64Image;

    String guestId;

    boolean voiceAction = false;


    private final MQTTClient mqttClient = new MQTTClient(this,this);



    boolean resetflag;

  //  private Handler emailhandler;

    private Runnable runnable;

    private boolean emailFlag;

    private String base64;

    private CountDownTimer timer;

    TextView textViewTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meet = findViewById(R.id.meet);
        video = findViewById(R.id.video);

        meet.setVisibility(View.GONE);


        video.setOnCompletionListener(mediaPlayer -> {
            if (voiceFlag) {
                startSpeechRecognition();
                speechRetryCount = 0;
            }
            if (resetflag) {
                resetActivityDelay(30000);
            }
            if (resetactvity) {
                resetActivity();
            }
            if (flag) {
                showDepartmentListBottomSheet();
                flag = false;
            }
            if (dialogFlag) {
                actionflag = true;
                showDialog();
                dialogFlag = false;
            }
            if (yesNoFlag) {
                toggle = true;
                showYesOrNoDialog();
                yesNoFlag = false;
            }
            if (aftersubmitflag) {
                resetActivity();
        }
        });

        meet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.department;
                playVideo(videoPath);
                emailFlag = true;
                flag = true;
                voiceAction = true;
                meet.setVisibility(View.GONE);
                isFaceDetected = false;
            }
        });


        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        if (allPermissionsGranted()) {
           startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    private void resetActivityDelay(int delay) {
        activityDelayHandler.removeCallbacks(activityDelayRunnable);
        activityDelayHandler.postDelayed(activityDelayRunnable, delay);
    }

    private void resetActivity() {
        activityDelayHandler.removeCallbacks(activityDelayRunnable);
        activityDelayRunnable.run();
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Log.d("string", " pressed");
//    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0).toLowerCase();
                    Log.d("Generated Speech",result);
              //      Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();
                    findVoiceAction(result);
                } else {
                    retrySpeechRecognition();
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onError(int error) {
                Log.e("Speech Recognition Error", "Error code: " + error);
                retrySpeechRecognition();
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0).toLowerCase();
                    Log.d("Partial Result",result);
//                    Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();
//                    findVoiceAction(result);
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        speechRecognizer.startListening(intent);
    }

    private void stopSpeechRecognition() {
        if (speechRecognizer != null) {
            // Stop listening
            speechRecognizer.stopListening();
            // Cancel pending speech recognition requests
            speechRecognizer.cancel();
            // Destroy the speech recognizer
            speechRecognizer.destroy();
        }
    }


    private void retrySpeechRecognition() {
        if (speechRetryCount < 7) {
            speechRetryCount++;
            startSpeechRecognition();
        } else {
            speechRetryCount = 0;
            Toast.makeText(MainActivity.this, "Speech recognition failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();

        FaceDetector detector = FaceDetection.getClient(options);

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            try {
                if (imageProxy.getImage() == null || isDetecting.get()) {
                    imageProxy.close();
                    return;
                }

                InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                detector.process(image)
                        .addOnSuccessListener(faces -> {
                            if (!faces.isEmpty() && isDetecting.compareAndSet(false, true)) {

                                voiceFlag = true;
                                startFlag = true;
                                ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

                                resetflag = true;

                                base64 = base64Image.replaceAll("\n", " ");

                                if(base64 != "")
                                {
                                    generateGuestId(base64);
                                }

                                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.welcome;
                                playVideo(videoPath);
                                video.setVisibility(View.VISIBLE);
                                meet.setVisibility(View.VISIBLE);
                            }
                        })
                        .addOnCompleteListener(task -> imageProxy.close());
            } catch (Exception e) {
                imageProxy.close();
            }
        });


        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }


    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void showDialog() {

        // Create a custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialogbox, null);

        // Set the message
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        if(emailFlag)
        {
            messageTextView.setText("Are you sure you want to meet "+showSelectedPerson()+"?");
        }
        else {
            messageTextView.setText("Are you sure you want to deliver?");
        }


        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);


        yesOrNoDialog = builder.create();
        yesOrNoDialog.setCanceledOnTouchOutside(false);

        buttonYes = dialogView.findViewById(R.id.yesButton);
        buttonNo = dialogView.findViewById(R.id.noButton);
        buttonYes.setOnClickListener(view -> {
            yesOrNoDialog.dismiss();
            actionflag = false;
            dialogFlag = false;
            if(emailFlag)
            {
                showEmailDialog();
            }
        });

        buttonNo.setOnClickListener(view -> {
            yesOrNoDialog.dismiss();
            actionflag = false;
            dialogFlag = false;
            if(emailFlag)
            {
                meet.performClick();
            }
        });

        yesOrNoDialog.show();
    }



    public void showEmailDialog() {

        submitFlag = true;

        View emailView = getLayoutInflater().inflate(R.layout.email_box, null);

        name = emailView.findViewById(R.id.name);
        purpose = emailView.findViewById(R.id.purpose);
        company = emailView.findViewById(R.id.company);
        mobile = emailView.findViewById(R.id.mobile);

        // Set up the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(emailView);

        // Create the AlertDialog
        emailFormAlert = builder.create();

        emailFormAlert.setCanceledOnTouchOutside(false);

        resetflag = false;

        resetActivityDelay(60000);

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.form;
        playVideo(videoPath);

        // Show the AlertDialog

        emailFormAlert.show();


        btnSubmit = emailView.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> {
            // Handle the submit button click
            String guestName = Objects.requireNonNull(name.getText()).toString();
            String purposeOfVisit = Objects.requireNonNull(purpose.getText()).toString();
            String companyName = Objects.requireNonNull(company.getText()).toString();
            String phoneNumber = Objects.requireNonNull(mobile.getText()).toString();

            hideKeyboard(purpose);


            if (!guestName.isEmpty() && !purposeOfVisit.isEmpty() && !companyName.isEmpty() && !phoneNumber.isEmpty()) {
                activityDelayHandler.removeCallbacks(activityDelayRunnable);
                String videoPath1 = "android.resource://" + getPackageName() + "/" + R.raw.checking;
                playVideo(videoPath1);

               aftersubmitflag = true;

     //           mqttflag = true;

                mqttClient.connect();

                submitFlag = false;
                voiceFlag = false;
                stopSpeechRecognition();
                sendEmail(emp_id, guestId, purposeOfVisit, guestName, companyName, phoneNumber);
                emailFormAlert.dismiss();
            }

            else {
                String videoPath1 = "android.resource://" + getPackageName() + "/" + R.raw.form;
                playVideo(videoPath1);
            }
        });

    }

    private void showDepartmentListBottomSheet() {

        meet.setVisibility(View.GONE);
        bottomSheetFlag = true;
        startFlag = false;

        // Create a bottom sheet dialog
        departmentBottomSheetDialog = new BottomSheetDialog(this);
        View departmentBottomSheetView = getLayoutInflater().inflate(R.layout.persons_sheet, null);
        departmentBottomSheetDialog.setContentView(departmentBottomSheetView);

        TextView text = departmentBottomSheetView.findViewById(R.id.txt);
        text.setText("Choose a Department");
        text.setTypeface(null, Typeface.BOLD_ITALIC); // Change text style here


        departmentBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(bottomSheetFlag)
                    departmentBottomSheetDialog.show();
            }
        });

        // Set up the ListView with the list of departments
        ListView departmentListView = departmentBottomSheetView.findViewById(R.id.listViewPersons);
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, departments);
        departmentListView.setAdapter(departmentAdapter);

        // Set item click listener for the department ListView
        departmentListView.setOnItemClickListener((adapterView, view, position, id) -> {

            bottomSheetFlag = false;

            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meet;
            playVideo(videoPath);

            departmentBottomSheetDialog.dismiss();
            showPersonListBottomSheet(position);

        });
        departmentBottomSheetDialog.show();
    }


    private void showPersonListBottomSheet(int selectedDepartment) {

        bottomSheet = true;

        departmentIndex = selectedDepartment;

        // Find the corresponding people list based on selected department
        persons = departmentPersons[selectedDepartment];

        // Create a bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.persons_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.setCanceledOnTouchOutside(false);

        TextView text = bottomSheetView.findViewById(R.id.txt);

        if(emailFlag){
            text.setText("Who would you like to Meet?");
            text.setTypeface(null, Typeface.BOLD_ITALIC); // Change text style here
        }


        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            if(bottomSheet) {
                bottomSheet = false;
                showDepartmentListBottomSheet();
            }
        });


        // Set up the ListView with the list of persons
        ListView listView = bottomSheetView.findViewById(R.id.listViewPersons);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, persons);
        listView.setAdapter(adapter);

        // Set item click listener for the ListView
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            bottomSheet = false;
            bottomSheetDialog.dismiss();

            this.selectedPerson = persons[position];
            this.email = email;
            emp_id = employee_id[selectedDepartment][position];

            dialogFlag = true;

            if(emailFlag)
            {
                findActivity(selectedPerson);
            }
            else {
                showDialog();
                actionflag = true;
            }
        });

        // Show the bottom sheet
        bottomSheetDialog.show();
    }


    public String showSelectedPerson()
    {
        return selectedPerson;
    }


    public void findActivity(String name)
    {
        String result = name.toLowerCase().trim();
        if(result.contains("farsana"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.farsana;
            playVideo(videoPath);
        }
        else if(result.contains("hameed"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.hameed;
            playVideo(videoPath);
        }
        else if(result.contains("shamsudheen"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shamsudheen;
            playVideo(videoPath);
        }
        else if(result.contains("namshid"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.namshid;
            playVideo(videoPath);
        }

        else if(result.contains("siddique pallathil"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.siddique;
            playVideo(videoPath);
        }
        else if(result.contains("jamal kp"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jamal;
            playVideo(videoPath);
        }
        else if(result.contains("amina"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.amina;
            playVideo(videoPath);
        }
        else if(result.contains("faris"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.faris;
            playVideo(videoPath);
        }
        else if(result.contains("sayed"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sayed;
            playVideo(videoPath);
        }
        else if(result.contains("alex ninan"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.alex;
            playVideo(videoPath);
        }
        else if(result.contains("muhammed nizar"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nizarkomath;
            playVideo(videoPath);
        }
        else if(result.contains("sandeep"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sandeep;
            playVideo(videoPath);
        }
        else if(result.contains("sareena"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sareena;
            playVideo(videoPath);
        }
        else if(result.contains("muhammed fayiz"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.fayiz;
            playVideo(videoPath);
        }
        else if(result.contains("arshad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.arshad;
            playVideo(videoPath);
        }
        else if(result.contains("ishack"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ishack;
            playVideo(videoPath);
        }
        else if(result.contains("haja shake"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.haja;
            playVideo(videoPath);
        }
        else if(result.contains("lijo"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.lijo;
            playVideo(videoPath);
        }
        else if(result.contains("sanid"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sanid;
            playVideo(videoPath);
        }
        else if(result.contains("ashique"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ashique;
            playVideo(videoPath);
        }
        else if(result.contains("anvar"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.anvar;
            playVideo(videoPath);
        }
        else if(result.contains("karla"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.karla;
            playVideo(videoPath);
        }
        else if(result.contains("sabith"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sabith;
            playVideo(videoPath);
        }
        else if(result.contains("salam"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.salam;
            playVideo(videoPath);
        }
        else if(result.contains("fajis"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.fajis;
            playVideo(videoPath);
        }
        else if(result.contains("firoz p mohamad ali"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.firoz;
            playVideo(videoPath);
        }
        else if(result.contains("muhammed ashfaq k.k"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ashfaq;
            playVideo(videoPath);
        }
        else if(result.contains("faisal abbas"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.faisal;
            playVideo(videoPath);
        }
        else if(result.contains("ansil.t.hameed"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ansil;
            playVideo(videoPath);
        }
        else if(result.contains("mujeeb rahman.k"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.mujeeb;
            playVideo(videoPath);
        }
        else if(result.contains("shanavas p.k"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shanavas;
            playVideo(videoPath);
        }
        else if(result.contains("subuhathali.m"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.subuhathali;
            playVideo(videoPath);
        }
        else if(result.contains("arshad hameed"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.arshad;
            playVideo(videoPath);
        }
        else if(result.contains("mohan kumar rai"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.kumar;
            playVideo(videoPath);
        }
        else if(result.contains("tariq"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.tariq;
            playVideo(videoPath);
        }
        else if(result.contains("thanzil"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.thanzil;
            playVideo(videoPath);
        }
        else if(result.contains("manu prasad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.manu;
            playVideo(videoPath);
        }
        else if(result.contains("askar pp"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.askar;
            playVideo(videoPath);
        }
        else if(result.contains("mansoor"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.mansoor;
            playVideo(videoPath);
        }
        else if(result.contains("yasser vm"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.yasser;
            ;
            playVideo(videoPath);
        }
        else if(result.contains("ashique c"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ashiq;
            playVideo(videoPath);
        }
        else if(result.contains("sumesh mohanan"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sumesh;
            playVideo(videoPath);
        }
        else if(result.contains("anwar"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.anwar;
            playVideo(videoPath);
        }
        else if(result.contains("arafath"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.arafath;
            playVideo(videoPath);
        }
        else if(result.contains("yoonus"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.yoonus;
            playVideo(videoPath);
        }
        else if(result.contains("ma jaleel"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jaleel;
            playVideo(videoPath);
        }
        else if(result.contains("abdul savad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.savad;
            playVideo(videoPath);
        }
        else if(result.contains("muhammed mk"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.muhammed;
            playVideo(videoPath);
        }
        else if(result.contains("muhammed ali komath"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ali;
            playVideo(videoPath);
        }
        else if(result.contains("saleesh"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.saleesh;
            playVideo(videoPath);
        }
        else if(result.contains("rashad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.rashad;
            playVideo(videoPath);
        }
        else if(result.contains("mohan"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.mohan;
            playVideo(videoPath);
        }
        else if(result.contains("hashim"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.hashim;
            playVideo(videoPath);
        } else if(result.contains("shahad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shahad;
            playVideo(videoPath);
        }
        else if(result.contains("jeremy"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jeremy;
            playVideo(videoPath);
        }
        else if(result.contains("abdullah"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.abdullah;
            playVideo(videoPath);
        }
        else if(result.contains("sajeer e"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sajeer;
            playVideo(videoPath);
        }
        else if(result.contains("afsal"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.afsal;
            playVideo(videoPath);
        }
        else if(result.contains("shahul"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shahul;
            playVideo(videoPath);
        }
        else if(result.contains("subhan"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.subhan;
            playVideo(videoPath);
        }
        else if(result.contains("krishna raj"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.krishna;
            playVideo(videoPath);
        }
        else if(result.contains("nizar komath"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nizarkomath;
            playVideo(videoPath);
        }
        else if(result.contains("risad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.risad;
            playVideo(videoPath);
        }
        else if(result.contains("noushad kt"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.noushad;
            playVideo(videoPath);
        }
        else if(result.contains("favas"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.favas;
            playVideo(videoPath);
        }
        else if(result.contains("rashid a"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.rashid;
            playVideo(videoPath);
        }
        else if(result.contains("monica sapkota"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.monica;
            playVideo(videoPath);
        }
        else if(result.contains("junaid"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.junaid;
            playVideo(videoPath);
        }
        else if(result.contains("thwelhath kp"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.thwelhath;
            playVideo(videoPath);
        }
        else if(result.contains("shakir mohammed"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shakir;
            playVideo(videoPath);
        }
        else if(result.contains("shafeek ks"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shafeek;
            playVideo(videoPath);
        }
        else if(result.contains("siaddin sidhique"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.siadeen;
            playVideo(videoPath);
        }
        else if(result.contains("rafeek"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.rafeek;
            playVideo(videoPath);
        }
        else if(result.contains("nisar kp"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nisarkp;
            playVideo(videoPath);
        }
        else if(result.contains("salman"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.salman;
            playVideo(videoPath);
        }
        else if(result.contains("prathap"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.prathap;
            playVideo(videoPath);
        }
        else if(result.contains("lenish kannan"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.lenish;
            playVideo(videoPath);
        }
        else if(result.contains("shyam om"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shyam;
            playVideo(videoPath);
        }
        else if(result.contains("swadik"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.swadik;
            playVideo(videoPath);
        }
        else if(result.contains("vivek"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.swadik;
            playVideo(videoPath);
        }
        else if(result.contains("jovian"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.swadik;
            playVideo(videoPath);
        }

    }
    public void findVoiceAction(String action)
    {
        String result = action.toLowerCase().trim();
        if(startFlag && result.contains("meet")) {
            startFlag = false;
            bottomSheetFlag = false;
            if(bottomSheetDialog != null)
                bottomSheetDialog.dismiss();
            if(yesOrNoDialog != null)
                yesOrNoDialog.dismiss();
            meet.performClick();

        }

        else if(actionflag && (result.contains("yes") || result.contains("es") || result.contains("yeas")))
        {
            buttonYes.performClick();
        }
        else if(toggle && (result.contains("yes") || result.contains("es") || result.contains("yeas"))){
            btnYes.performClick();
        }
        else if(actionflag && (result.contains("no") || result.contains("noo")))
        {
            buttonNo.performClick();
        }
        else if(toggle && (result.contains("no") || result.contains("noo"))){
            btnNo.performClick();
        }
        else if(result.contains("submit") && submitFlag)
        {
            btnSubmit.performClick();
            blana = null;
        }
        else if(voiceAction && (result.contains("farsana")) && departmentIndex == 0)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.farsana;
            startVoiceAction(videoPath,"Farsana","nestogroup-942b-4c08-8d17-02732b96a2b4");
        }
        else if(voiceAction && (result.contains("hameed")) && departmentIndex == 5 )
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.hameed;
            startVoiceAction(videoPath,"Hameed","nestogroup-942b-4c08-8d17-02732b96a2b3");
        }
        else if(voiceAction && (result.contains("shamsudheen")) && departmentIndex == 5 )
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shamsudheen;
            startVoiceAction(videoPath,"Shamsudheen","nestogroup-942b-4c08-8d17-02732b96a2b2");
        }
        else if(voiceAction && (result.contains("namshid")) && departmentIndex == 5 )
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.namshid;
            startVoiceAction(videoPath,"Namshid","nestogroup-942b-4c08-8d17-02732b96a2b1");
        }
        else if(voiceAction && (result.contains("siddique")) && departmentIndex == 0)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.siddique;
            startVoiceAction(videoPath,"Siddique Pallathil","nestogroup-942b-4c08-8d17-02732b96a2b6");
        }
        else if(voiceAction && (result.contains("jamal")) && departmentIndex == 0)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jamal;
            startVoiceAction(videoPath,"Jamal KP","nestogroup-942b-4c08-8d17-02732b96a2b7");
        }
        else if(voiceAction && (result.contains("amina")) && departmentIndex == 0)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.amina;
            startVoiceAction(videoPath,"Amina","nestogroup-942b-4c08-8d17-02732b96a2b8");
        }
        else if(voiceAction && (result.contains("faris")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.faris;
            startVoiceAction(videoPath,"Faris","nestogroup-942b-4c08-8d17-02732b96a2b9");
        }
        else if(voiceAction && (result.contains("sayed")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sayed;
            startVoiceAction(videoPath,"Sayed","nestogroup-942b-4c08-8d17-02732b96a2b10");
        }
        else if(voiceAction && (result.contains("alex")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.alex;
            startVoiceAction(videoPath,"Alex ninan","nestogroup-942b-4c08-8d17-02732b96a2b11");
        }
        else if(voiceAction && (result.contains("nizar")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nizarkomath;
            startVoiceAction(videoPath,"Muhammed Nizar","nestogroup-942b-4c08-8d17-02732b96a2b12");
        }
        else if(voiceAction && (result.contains("sandeep")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sandeep;
            startVoiceAction(videoPath,"Sandeep","nestogroup-942b-4c08-8d17-02732b96a2b13");
        }
        else if(voiceAction && (result.contains("sareena")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sareena;
            startVoiceAction(videoPath,"Sareena","nestogroup-942b-4c08-8d17-02732b96a2b14");
        }
        else if(voiceAction && (result.contains("fayiz")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.fayiz;
            startVoiceAction(videoPath,"Muhammed Fayiz","nestogroup-942b-4c08-8d17-02732b96a2b15");
        }
        else if(voiceAction && (result.contains("arshad")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.arshad;
            startVoiceAction(videoPath,"Arshad","nestogroup-942b-4c08-8d17-02732b96a2b16");
        }
        else if(voiceAction && (result.contains("ishack")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ishack;
            startVoiceAction(videoPath,"Ishack","nestogroup-942b-4c08-8d17-02732b96a2b17");
        }
        else if(voiceAction && (result.contains("haja") || result.contains("shake")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.haja;
            startVoiceAction(videoPath,"Haja Shake","nestogroup-942b-4c08-8d17-02732b96a2b18");
        }
        else if(voiceAction && (result.contains("lijo")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.lijo;
            startVoiceAction(videoPath,"Lijo","nestogroup-942b-4c08-8d17-02732b96a2b19");
        }
        else if(voiceAction && (result.contains("sanid")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sanid;
            startVoiceAction(videoPath,"Sanid","nestogroup-942b-4c08-8d17-02732b96a2b20");
        }
        else if(voiceAction && (result.contains("ashique")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ashique;
            startVoiceAction(videoPath,"Ashique","nestogroup-942b-4c08-8d17-02732b96a2b21");
        }
        else if(voiceAction && (result.contains("anvar")) && departmentIndex == 2)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.anvar;
            startVoiceAction(videoPath,"Anvar","nestogroup-942b-4c08-8d17-02732b96a2b22");
        }
        else if(voiceAction && (result.contains("karla")) && departmentIndex == 3)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.karla;
            startVoiceAction(videoPath,"Karla","nestogroup-942b-4c08-8d17-02732b96a2b23");
        }
        else if(voiceAction && (result.contains("sabith")) && departmentIndex == 4)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sabith;
            startVoiceAction(videoPath,"Sabith","nestogroup-942b-4c08-8d17-02732b96a2b24");
        }
        else if(voiceAction && (result.contains("salam")) && departmentIndex == 4)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.salam;
            startVoiceAction(videoPath,"Salam","nestogroup-942b-4c08-8d17-02732b96a2b25");
        }
        else if(voiceAction && (result.contains("fajis")) && departmentIndex == 4)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.fajis;
            startVoiceAction(videoPath,"Fajis","nestogroup-942b-4c08-8d17-02732b96a2b26");
        }
        else if(voiceAction && (result.contains("firoz")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.firoz;
            startVoiceAction(videoPath,"Firoz P Mohamad Ali","nestogroup-942b-4c08-8d17-02732b96a2b27");
        }
        else if(voiceAction && (result.contains("ashfaq")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ashfaq;
            startVoiceAction(videoPath,"Muhammed Ashfaq K.K","nestogroup-942b-4c08-8d17-02732b96a2b28");
        }
        else if(voiceAction && (result.contains("abbas")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.faisal;
            startVoiceAction(videoPath,"Faisal Abbas","nestogroup-942b-4c08-8d17-02732b96a2b29");
        }
        else if(voiceAction && (result.contains("ansil")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.hameed;
            startVoiceAction(videoPath,"Ansil.T.Hameed","nestogroup-942b-4c08-8d17-02732b96a2b30");
        }
        else if(voiceAction && (result.contains("mujeeb")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.mujeeb;
            startVoiceAction(videoPath,"Mujeeb Rahman.K","nestogroup-942b-4c08-8d17-02732b96a2b31");
        }
        else if(voiceAction && (result.contains("shanavas")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shanavas;
            startVoiceAction(videoPath,"Shanavas P.K","nestogroup-942b-4c08-8d17-02732b96a2b32");
        }
        else if(voiceAction && (result.contains("subuhath")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.subuhathali;
            startVoiceAction(videoPath,"Subuhathali.M","nestogroup-942b-4c08-8d17-02732b96a2b33");
        }
        else if(voiceAction && (result.contains("arshad")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.arshad;
            startVoiceAction(videoPath,"Arshad Hameed","nestogroup-942b-4c08-8d17-02732b96a2b34");
        }
        else if(voiceAction && (result.contains("kumar rai")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.kumar;
            startVoiceAction(videoPath,"Mohan Kumar Rai","nestogroup-942b-4c08-8d17-02732b96a2b35");
        }
        else if(voiceAction && (result.contains("tariq")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.tariq;
            startVoiceAction(videoPath,"Tariq","nestogroup-942b-4c08-8d17-02732b96a2b36");
        }
        else if(voiceAction && (result.contains("thanzil")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.thanzil;
            startVoiceAction(videoPath,"Thanzil","nestogroup-942b-4c08-8d17-02732b96a2b37");
        }
        else if(voiceAction && (result.contains("manu prasad")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.manu;
            startVoiceAction(videoPath,"Manu Prasad","nestogroup-942b-4c08-8d17-02732b96a2b38");
        }
        else if(voiceAction && (result.contains("askar")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.askar;
            startVoiceAction(videoPath,"Askar pp","nestogroup-942b-4c08-8d17-02732b96a2b39");
        }
        else if(voiceAction && (result.contains("mansoor")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.mansoor;
            startVoiceAction(videoPath,"Mansoor","nestogroup-942b-4c088d17-02732b96a2b40");
        }
        else if(voiceAction && (result.contains("yasser")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.yasser;
            startVoiceAction(videoPath,"Yasser VM","nestogroup-942b-4c08-8d17-02732b96a2b41");
        }
        else if(voiceAction && (result.contains("ashi")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ashiq;
            startVoiceAction(videoPath,"Ashique C","nestogroup-942b-4c08-8d17-02732b96a2b42");
        }
        else if(voiceAction && (result.contains("sumesh")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sumesh;
            startVoiceAction(videoPath,"Sumesh Mohanan","nestogroup-942b-4c08-8d17-02732b96a2b43");
        }
        else if(voiceAction && (result.contains("anwar")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.anwar;
            startVoiceAction(videoPath,"Anwar","nestogroup-942b-4c08-8d17-02732b96a2b44");
        }
        else if(voiceAction && (result.contains("arafath")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.arafath;
            startVoiceAction(videoPath,"Arafath","nestogroup-942b-4c08-8d17-02732b96a2b45");
        }
        else if(voiceAction && (result.contains("yoonus")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.yoonus;
            startVoiceAction(videoPath,"Yoonus","nestogroup-942b-4c08-8d17-02732b96a2b46");
        }
        else if(voiceAction && (result.contains("jaleel")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jaleel;
            startVoiceAction(videoPath,"MA Jaleel","nestogroup-942b-4c08-8d17-02732b96a2b47");
        }
        else if(voiceAction && (result.contains("abdul savad")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.savad;
            startVoiceAction(videoPath,"Abdul Savad","nestogroup-942b-4c08-8d17-02732b96a2b48");
        }
        else if(voiceAction && (result.contains("muhammed")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.muhammed;
            startVoiceAction(videoPath,"Muhammed MK","nestogroup-942b-4c08-8d17-02732b96a2b49");
        }
        else if(voiceAction && (result.contains("ali")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ali;
            startVoiceAction(videoPath,"Muhammed Ali Komath","nestogroup-942b-4c08-8d17-02732b96a2b50");
        }
        else if(voiceAction && (result.contains("saleesh")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.saleesh;
            startVoiceAction(videoPath,"Saleesh","nestogroup-942b-4c08-8d17-02732b96a2b51");
        }
        else if(voiceAction && (result.contains("rashad")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.rashad;
            startVoiceAction(videoPath,"Rashad","nestogroup-942b-4c08-8d17-02732b96a2b52");
        }
        else if(voiceAction && (result.contains("mohan")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.mohan;
            startVoiceAction(videoPath,"Mohan","nestogroup-942b-4c08-8d17-02732b96a2b53");
        }
        else if(voiceAction && (result.contains("hashim")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.hashim;
            startVoiceAction(videoPath,"Hashim","nestogroup-942b-4c08-8d17-02732b96a2b54");
        }
        else if(voiceAction && (result.contains("shahad")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shahad;
            startVoiceAction(videoPath,"Shahad","nestogroup-942b-4c08-8d17-02732b96a2b55");
        }
        else if(voiceAction && (result.contains("jeremy")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jeremy;
            startVoiceAction(videoPath,"Jeremy","nestogroup-942b-4c08-8d17-02732b96a2b56");
        }
        else if(voiceAction && (result.contains("abdullah")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.abdullah;
            startVoiceAction(videoPath,"Abdullah","nestogroup-942b-4c08-8d17-02732b96a2b57");
        }
        else if(voiceAction && (result.contains("sajeer")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sajeer;
            startVoiceAction(videoPath,"Sajeer E","nestogroup-942b-4c08-8d17-02732b96a2b58");
        }
        else if(voiceAction && (result.contains("afsal")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.namshid;
            startVoiceAction(videoPath,"Afsal","nestogroup-942b-4c088d17-02732b96a2b59");
        }
        else if(voiceAction && (result.contains("shahul")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shahul;
            startVoiceAction(videoPath,"Shahul","nestogroup-942b-4c08-8d17-02732b96a2b60");
        }
        else if(voiceAction && (result.contains("ramsheed")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ramsheed;
            startVoiceAction(videoPath,"ramsheed","nestogroup-942b-4c08-8d17-02732b96a2b61");
        }
        else if(voiceAction && (result.contains("subhan")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.subhan;
            startVoiceAction(videoPath,"Subhan","nestogroup-942b-4c08-8d17-02732b96a2b62");
        }
        else if(voiceAction && (result.contains("krishna")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.krishna;
            startVoiceAction(videoPath,"Krishna Raj","nestogroup-942b-4c08-8d17-02732b96a2b63");
        }
        else if(voiceAction && (result.contains("komath")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nizarkomath;
            startVoiceAction(videoPath,"Nizar Komath","nestogroup-942b-4c08-8d17-02732b96a2b64");
        }
        else if(voiceAction && (result.contains("risad")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.risad;
            startVoiceAction(videoPath,"Risad","nestogroup-942b-4c08-8d17-02732b96a2b65");
        }
        else if(voiceAction && (result.contains("noushad")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.noushad;
            startVoiceAction(videoPath,"Noushad KT","nestogroup-942b-4c08-8d17-02732b96a2b66");
        }
        else if(voiceAction && (result.contains("favas")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.favas;
            startVoiceAction(videoPath,"Favas","nestogroup-942b-4c08-8d17-02732b96a2b67");
        }
        else if(voiceAction && (result.contains("rashid")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.rashid;
            startVoiceAction(videoPath,"Rashid A","nestogroup-942b-4c08-8d17-02732b96a2b68");
        }
        else if(voiceAction && (result.contains("monica")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.monica;
            startVoiceAction(videoPath,"Monica Sapkota","nestogroup-942b-4c08-8d17-02732b96a2b69");
        }
        else if(voiceAction && (result.contains("junaid")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.junaid;
            startVoiceAction(videoPath,"Junaid","nestogroup-942b-4c08-8d17-02732b96a2b70");
        }
        else if(voiceAction && (result.contains("thwelhath")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.thwelhath;
            startVoiceAction(videoPath,"Thwelhath KP","nestogroup-942b-4c08-8d17-02732b96a2b71");
        }
        else if(voiceAction && (result.contains("shakir")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shakir;
            startVoiceAction(videoPath,"Shakir Mohammed","nestogroup-942b-4c08-8d17-02732b96a2b72");
        }
        else if(voiceAction && (result.contains("shafeek")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shafeek;
            startVoiceAction(videoPath,"Shafeek KS","nestogroup-942b-4c08-8d17-02732b96a2b73");
        }
        else if(voiceAction && (result.contains("siaddin")) && departmentIndex == 5)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.siadeen;
            startVoiceAction(videoPath,"Siaddin Sidhique","nestogroup-942b-4c08-8d17-02732b96a2b74");
        }
        else if(voiceAction && (result.contains("rafeek")) && departmentIndex == 6)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.rafeek;
            startVoiceAction(videoPath,"Rafeek","nestogroup-942b-4c08-8d17-02732b96a2b75");
        }
        else if(voiceAction && (result.contains("nisar")) && departmentIndex == 6)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nisarkp;
            startVoiceAction(videoPath,"Nisar KP","nestogroup-942b-4c08-8d17-02732b96a2b76");
        }
        else if(voiceAction && (result.contains("salman")) && departmentIndex == 6)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.salman;
            startVoiceAction(videoPath,"Salman","nestogroup-942b-4c08-8d17-02732b96a2b77");
        }
        else if(voiceAction && (result.contains("prathap")) && departmentIndex == 6)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.prathap;
            startVoiceAction(videoPath,"Prathap","nestogroup-942b-4c08-8d17-02732b96a2b78");
        }
        else if(voiceAction && (result.contains("lenish")) && departmentIndex == 7)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.lenish;
            startVoiceAction(videoPath,"Lenish Kannan","nestogroup-942b-4c08-8d17-02732b96a2b79");
        }
        else if(voiceAction && (result.contains("shyam")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shyam;
            startVoiceAction(videoPath,"Shyam OM","nestogroup-942b-4c08-8d17-02732b96a2b80");
        }
        else if(voiceAction && (result.contains("swadik")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.swadik;
            startVoiceAction(videoPath,"Swadik","nestogroup-942b-4c08-8d17-02732b96a2b81");
        }
        else if(voiceAction && (result.contains("swadik")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.swadik;
            startVoiceAction(videoPath,"vivek","261daf56-0287-43fe-9c13-93f295a3c371");
        }
        else if(voiceAction && (result.contains("jovian")) && departmentIndex == 1)
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.swadik;
            startVoiceAction(videoPath,"jovian","bbac478c-ff0f-40db-b285-35c8ac8c38ae");
        }
        else {
            startSpeechRecognition();
            speechRetryCount++;
        }

    }

    public void startVoiceAction(String videoPath,String name,String emp_id)
    {
        if(emailFlag)
        {
            playVideo(videoPath);
            dialogFlag = true;
        }
        else
        {
            showDialog();
            startSpeechRecognition();
            actionflag = true;
        }
        voiceAction = false;
        bottomSheet = false;
        bottomSheetFlag = false;
        bottomSheetDialog.dismiss();
        selectedPerson = name;
        this.emp_id = emp_id;
    }

    private void sendEmail(String employeeId, String guestId, String purposeOfVisit, String guestName, String companyName, String phoneNumber) {

        apiCaller = new ApiCaller();
        apiCaller.executeApiCall(employeeId, guestId, purposeOfVisit, guestName, companyName, phoneNumber);


        // Schedule the runnable to be executed after the delay
        resetActivityDelay(120000);

    }


    public void playVideo(String path)
    {
        stopSpeechRecognition();
        activityDelayHandler.removeCallbacks(activityDelayRunnable);
        video.setVideoURI(Uri.parse(path));
        video.start();
    }

    private void hideKeyboard(TextInputEditText purpose) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(purpose.getWindowToken(), 0);
        }
    }


    @Override
    public void onApiResult(String result) {

        try {
            JSONObject jsonResponse = new JSONObject(result);

            guestId = jsonResponse.getJSONObject("data")
                    .getJSONObject("guest")
                    .getString("guest_id");
            Log.d("result object", "Received guest_id: " + result);
            // Now you can use the guestId as needed in your activity
            Log.d("result object", "Received guest_id: " + jsonResponse);
            Log.d("result object", "Received guest_id: " + guestId);
        } catch (Exception e) {
            Log.e("TAG", "Error parsing JSON response", e);
        }

    }


    @Override
    public void onMessageReceived(String topic, String message) {

        Log.d("topic", topic);

        Log.d("mqtt response", message);

        aftersubmitflag = false;

        resetflag = true;

        mqttflag = false;

        activityDelayHandler.removeCallbacks(activityDelayRunnable);

        if (message != null && message.toLowerCase().contains("accepted")) {
            voiceFlag = false;
            resetactvity = true;
            mqttClient.disconnect();
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.available;
            playVideo(videoPath);
        } else if (message != null && message.toLowerCase().contains("rejected")) {
            yesNoFlag = true;
            voiceFlag = true;
            mqttClient.disconnect();
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.notavailable;
            playVideo(videoPath);
            generateGuestId(base64);
        }

    }



    private void showYesOrNoDialog()
    {
        View dialogView = getLayoutInflater().inflate(R.layout.yesorno_dialog, null);

        // Set the message
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        messageTextView.setText("Would you like to meet someone else?");


        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);


        dialogYesOrNo = builder.create();
        dialogYesOrNo.setCanceledOnTouchOutside(false);

        btnYes = dialogView.findViewById(R.id.yesbtn);
        btnNo = dialogView.findViewById(R.id.nobtn);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogYesOrNo.dismiss();
                toggle = false;
                if(emailFlag)
                {
                    meet.performClick();
                }

            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogYesOrNo.dismiss();
                voiceFlag = false;
                toggle = false;
                resetactvity = true;
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.thankyou;
                playVideo(videoPath);
            }
        });

        dialogYesOrNo.show();
    }

    private void generateGuestId(String base64)
    {
        GuestIdAPI getGuestId = new GuestIdAPI(this);
        getGuestId.retriveGuestId("",base64);
    }

}