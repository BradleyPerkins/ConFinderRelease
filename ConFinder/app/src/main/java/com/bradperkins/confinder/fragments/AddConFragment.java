package com.bradperkins.confinder.fragments;


import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.utils.DataHelper;
import com.bradperkins.confinder.utils.FormUtils;
import com.bradperkins.confinder.utils.ImageUtils;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddConFragment extends Fragment {

    private static final String ARG_OBJ = "ARG_OBJ";
    private static final String ARG_LIST = "ARG_LIST";
    private static final String ARG_POS = "ARG_POS";
    private static final String ARG_RESULT_OK = "ARG_RESULT_OK";

    private String mCurrentPhotoPath;
    private File photoFile = null;
    private Uri selectedImage = null;

    private static final int REQUEST_IMAGE_CAPTURE = 0x01001;
    private static final int REQUEST_GALLERY_PICK = 0x03001;
    private static final int REQUEST_CAMERA_PERMISSION = 0x02001;
    private static final int REQUEST_STORAGE_PERMISSION = 0x04001;

    private String monthStr;
    private int monthNum;
    private String dayStr;
    private String yearStr;

    private String startHourStr;
    private String startMinStr;
    private String startAPStr;
    private String endHourStr;
    private String endMinStr;
    private String endAPStr;

    private String day1 = "";
    private String day2 = "";
    private String day3 = "";
    private String day4 = "";

    private int RESULT_OK;

    private double id = 0;
    private String title = "";
    private String admin = "";
    private String address = "";
    private String building = "";
    private String city = "";
    private double zip = 0;
    private String stateAbrev = "";
    private String website = "";
    private String tickets = "";
    private int attending = 0;
    private int likes = 0;
    private String image = "";
    private double latitude = 0;
    private double longitude = 0;
    private String date = "";
    private String conId = "";

    private TextView day1TV, day2TV, day3TV, day4TV, statesHeaderTV;
    private EditText zipET, webET, ticketsET, addressET, cityET, titleET, venueET;
    private ImageView imageBtn;
    private ProgressBar progressBar;


    private AddConListener mListener;


    public AddConFragment() {
    }

    public static AddConFragment newInstance(ArrayList<Con> conList, int position, String objStr, int result) {
        Bundle args = new Bundle();
        args.putString(ARG_OBJ, objStr);
        args.putSerializable(ARG_LIST, conList);
        args.putInt(ARG_POS, position);
        args.putInt(ARG_RESULT_OK, result);
        AddConFragment fragment = new AddConFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_con, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Spinner stateSpinner = getView().findViewById(R.id.states_spinner);
        //State Spinner
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.states_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);

        ArrayList<Con> conList = (ArrayList<Con>) getArguments().getSerializable(ARG_LIST);
        int pos = getArguments().getInt(ARG_POS);
        String objStr = getArguments().getString(ARG_OBJ);
        RESULT_OK = getArguments().getInt(ARG_RESULT_OK);

        statesHeaderTV = getView().findViewById(R.id.state_tv_header);
        Button submitBtn = getView().findViewById(R.id.submit_con_btn);
        titleET = getView().findViewById(R.id.con_title_et);
        venueET = getView().findViewById(R.id.con_venue_et);
        addressET = getView().findViewById(R.id.con_address_et);
        cityET = getView().findViewById(R.id.con_city_et);
        zipET = getView().findViewById(R.id.con_zip_et);
        webET = getView().findViewById(R.id.con_url_et);
        ticketsET = getView().findViewById(R.id.con_tickets_et);
        imageBtn = getView().findViewById(R.id.add_con_image_btn);

        day1TV = getView().findViewById(R.id.con_time1_tv);
        day2TV = getView().findViewById(R.id.con_time2_tv);
        day3TV = getView().findViewById(R.id.con_time3_tv);
        day4TV = getView().findViewById(R.id.con_time4_tv);

        if (conList != null){
            String state = conList.get(pos).getState();
            String[] statesArr = getResources().getStringArray(R.array.states_array);
            int stateIndex = -1;
            for (int i=0;i<statesArr.length;i++) {
                if (statesArr[i].equals(state)) {
                    stateIndex = i;
                }
            }
            stateSpinner.setSelection(stateIndex);

            Glide.with(this).load(conList.get(pos).getImage()).into(imageBtn);
            titleET.setText(conList.get(pos).getTitle());
            venueET.setText(conList.get(pos).getBuilding());
            addressET.setText(conList.get(pos).getAddress());
            cityET.setText(conList.get(pos).getCity());
            zipET.setText((int) conList.get(pos).getZip() + "");
            webET.setText(conList.get(pos).getUrl());
            ticketsET.setText(conList.get(pos).getTickets());
            day1TV.setText(conList.get(pos).getHours1());
            day2TV.setText(conList.get(pos).getHours2());
            day3TV.setText(conList.get(pos).getHours3());
            day4TV.setText(conList.get(pos).getHours4());

            title = conList.get(pos).getTitle();
            building = conList.get(pos).getBuilding();
            address = conList.get(pos).getAddress();
            city = conList.get(pos).getCity();
            zip = conList.get(pos).getZip();
            website = conList.get(pos).getUrl();
            tickets = conList.get(pos).getTickets();
            day1 = conList.get(pos).getHours1();
            day2 = conList.get(pos).getHours2();
            day3 = conList.get(pos).getHours3();
            day4 = conList.get(pos).getHours4();
            id = conList.get(pos).getId();
            date = conList.get(pos).getDate();
            image = conList.get(pos).getImage();
            selectedImage = Uri.parse("edit");
            likes = conList.get(pos).getLikes();
            attending = conList.get(pos).getAttending();
            conId = conList.get(pos).getConid();
        }

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] states =  getResources().getStringArray(R.array.states_array);
                stateAbrev = states[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageDialog();
            }
        });

        day1TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(getContext(), 1);
            }
        });

        day2TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(getContext(), 2);

            }
        });

        day3TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(getContext(), 3);

            }
        });

        day4TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(getContext(), 4);

            }
        });

        //Submit send data through interface
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isOnline = DataHelper.hasNetwork(getContext());
                if (isOnline){
                    title = titleET.getText().toString().trim();
                    building = venueET.getText().toString().trim();
                    address = addressET.getText().toString().trim();
                    city = cityET.getText().toString().trim();
                    website = webET.getText().toString().trim();
                    tickets = ticketsET.getText().toString().trim();
                    day1 = day1.trim();
                    day2 = day2.trim();
                    day3 = day3.trim();
                    day4 = day4.trim();
                    conId = title + "-" + DataHelper.dateStamp();

                    if (!title.isEmpty() || !building.isEmpty() || !address.isEmpty() || !city.isEmpty() || !day1.isEmpty() || !zipET.getText().toString().trim().isEmpty()) {
                        if (!stateAbrev.equals("Choose State:")){
                            statesHeaderTV.setText("States");
                            statesHeaderTV.setTextColor(Color.BLACK);
                        }

                        if (FormUtils.zipCheck(zipET.getText().toString().trim())){
                            zip = Double.parseDouble(zipET.getText().toString().trim());
                            date = DataHelper.dateFormatter(id);
                            mListener.submitCon(title, building,
                                    address, city,
                                    stateAbrev, zip, website,
                                    tickets, day1, day2, day3, day4,
                                    image, id, attending, likes, date, selectedImage, conId);
                        }else {
                            zipET.setError("Please enter a valid Zip code");
                        }

                    }else {
                        if (title.isEmpty()){
                            titleET.setError("Please enter a valid Con Title");
                        }
                        if (building.isEmpty()){
                            venueET.setError("Please enter a valid Venue/Building Name");
                        }
                        if (address.isEmpty()){
                            addressET.setError("Please enter a valid address");
                        }
                        if (city.isEmpty()){
                            cityET.setError("Please enter a valid City");
                        }
                        if (zipET.getText().toString().trim().isEmpty()){
                            zipET.setError("Please enter a valid Zip code");
                        }

                        if (day1.isEmpty()){
                            day1TV.setError("Please enter in dates and times for at least 1 day");
                        }
                        if (stateAbrev.equals("Choose State:")){
                            statesHeaderTV.setText("*Please Choose State");
                            statesHeaderTV.setTextColor(Color.RED);
                        }
                    }

                } else{
                    Toast.makeText(getContext(), "Check your network Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == RESULT_OK){
                    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK &&
                            data != null && data.hasExtra("data")){
                        Bitmap cameraImage = (Bitmap)data.getParcelableExtra("data");
                        try {
                            cameraImage = ImageUtils.modifyOrientation(cameraImage, mCurrentPhotoPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        selectedImage = getImageUri(getContext(), cameraImage);
                        Glide.with(this).load(photoFile.getAbsolutePath()).into(imageBtn);

                    }else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK &&
                            (data == null || !data.hasExtra("data"))){
                        Bitmap cameraImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        try {
                            cameraImage = ImageUtils.modifyOrientation(cameraImage, mCurrentPhotoPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        selectedImage = getImageUri(getContext(), cameraImage);
                        Glide.with(this).load(photoFile.getAbsolutePath()).into(imageBtn);
                    }
                }
                break;
            case REQUEST_GALLERY_PICK:
                if(resultCode == RESULT_OK){
                    selectedImage = data.getData();
                    Glide.with(this).load(selectedImage).into(imageBtn);
                }
                break;
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        mCurrentPhotoPath = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(mCurrentPhotoPath);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddConListener){
            mListener = (AddConListener) context;
        }
    }

    public interface AddConListener {
        void submitCon(String title, String venue, String address,
                       String city, String state, double zip, String website,
                       String tickets, String day1, String day2, String day3,
                       String day4, String image, double id, int attending, int likes,
                       String date, Uri imgUri, String conId);
    }

    //Image Selection Dialog
    public void imageDialog(){
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_image_alert);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        final LinearLayout camera = dialog.findViewById(R.id.camera_add_image);
        LinearLayout gallery = dialog.findViewById(R.id.gallery_add_image);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (photoFile != null) {
                            Uri contentUri = FileProvider.getUriForFile(getActivity(),
                                    "com.bradperkins.confinder.fileprovider",
                                    photoFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] {Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
//
//                    ActivityCompat.requestPermissions(getActivity(),
//                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                            REQUEST_STORAGE_PERMISSION);
                }
                dialog.dismiss();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , REQUEST_GALLERY_PICK);
                }else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
//
//                    ActivityCompat.requestPermissions(getActivity(),
//                            new String[] {Manifest.permission.CAMERA},
//                            REQUEST_CAMERA_PERMISSION);
                }

                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public void showDialog(Context context, final int day){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_add_con_day);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final String[] months = context.getResources().getStringArray(R.array.month);
        final String[] days = context.getResources().getStringArray(R.array.days);
        final String[] year = {"2019", "2020"};
        final String[] amPm = {"am", "pm"};
        final String[] hour = context.getResources().getStringArray(R.array.hour);
        final String[] min = context.getResources().getStringArray(R.array.minute);

        //Month
        Spinner monthSpinner = dialog.findViewById(R.id.month_spin);
        ArrayAdapter<String> monthAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, months);
        monthAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdpater);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                monthNum = i + 1;
                monthStr = months[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Day
        Spinner daySpinner = dialog.findViewById(R.id.day_spin);
        ArrayAdapter<String> dayAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, days);
        dayAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdpater);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dayStr = days[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //Year
        Spinner yearSpinner = dialog.findViewById(R.id.year_spin);
        ArrayAdapter<String> yearAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, year);
        yearAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdpater);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                yearStr = year[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //////////////////////
        //Start Hour
        Spinner startHourSpin = dialog.findViewById(R.id.start_hour_spin);
        ArrayAdapter<String> startHourAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, hour);
        startHourAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startHourSpin.setAdapter(startHourAdpater);
        startHourSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                startHourStr = hour[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //Start Min
        Spinner startMinSpin = dialog.findViewById(R.id.start_min_spin);
        ArrayAdapter<String> startMinAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, min);
        startMinAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startMinSpin.setAdapter(startMinAdpater);
        startMinSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                startMinStr = min[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //Start am/pm
        Spinner startAmPmSpin = dialog.findViewById(R.id.start_am_pm_spin);
        ArrayAdapter<String> startAmPmAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, amPm);
        startAmPmAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startAmPmSpin.setAdapter(startAmPmAdpater);
        startAmPmSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                startAPStr = amPm[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        /////////////////////
        //End Hour
        Spinner endHourSpin = dialog.findViewById(R.id.end_hour_spin);
        ArrayAdapter<String> endHourAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, hour);
        endHourAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endHourSpin.setAdapter(endHourAdpater);
        endHourSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                endHourStr = hour[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        //End Min
        Spinner endMinSpin = dialog.findViewById(R.id.end_min_spin);
        ArrayAdapter<String> endMinAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, min);
        endMinAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endMinSpin.setAdapter(endMinAdpater);
        endMinSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                endMinStr = min[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //End am/pm
        Spinner endAmPmSpin = dialog.findViewById(R.id.end_am_pm_spin);
        ArrayAdapter<String> endAmPmAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, amPm);
        endAmPmAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endAmPmSpin.setAdapter(endAmPmAdpater);
        endAmPmSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                endAPStr = amPm[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //Submit
        Button submitButton = dialog.findViewById(R.id.submit_con_dialog_btn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if day is 1
                if (day == 1){
                    id = Integer.parseInt(monthNum + dayStr + yearStr);
                    id = idBuilder(monthNum, dayStr, yearStr);
                    day1 = monthStr + " " + dayStr + ", " + yearStr + "   "
                            + startHourStr + ":" + startMinStr + " " + startAPStr + " - "
                            + endHourStr + ":" + endMinStr + " " + endAPStr;
                    day1TV.setText(day1);

                } else if (day == 2){
                    day2 = monthStr + " " + dayStr + ", " + yearStr + "   "
                            + startHourStr + ":" + startMinStr + " " + startAPStr + " - "
                            + endHourStr + ":" + endMinStr + " " + endAPStr;
                    day2TV.setText(day2);
                } else if (day == 3){
                    day3 = monthStr + " " + dayStr + ", " + yearStr + "   "
                            + startHourStr + ":" + startMinStr + " " + startAPStr + " - "
                            + endHourStr + ":" + endMinStr + " " + endAPStr;
                    day3TV.setText(day3);
                }else if (day == 4){
                    day4 = monthStr + " " + dayStr + ", " + yearStr + "   "
                            + startHourStr + ":" + startMinStr + " " + startAPStr + " - "
                            + endHourStr + ":" + endMinStr + " " + endAPStr;
                    day4TV.setText(day4);
                }
                dialog.dismiss();
            }
        });


        //Cancel
        Button dialogButton = dialog.findViewById(R.id.cancel_con_dialog_btn);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    private int idBuilder(int month, String day, String year){
        int id = 0;
        int d = Integer.parseInt(day);
        String dayStr;
        String monthStr = String.valueOf(month);

        if (d < 10){
            dayStr = "0" + String.valueOf(d);
        } else {
            dayStr = String.valueOf(d);
        }

        String yearStr1 = String.valueOf(year);
        yearStr1 = yearStr1.substring(2);

        id = Integer.parseInt(monthStr + dayStr + yearStr1);

        //combine and convert to int
        return id;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
