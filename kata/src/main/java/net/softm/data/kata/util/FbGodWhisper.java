/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.softm.data.kata.util;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.dto.base.Org;
import net.softm.data.kata.god.dto.PrayClass;
import net.softm.data.kata.god.dto.PrayClassParty;
import net.softm.data.kata.god.dto.PrayComment;
import net.softm.data.kata.god.dto.PrayMemo;
import net.softm.data.kata.god.dto.PrayPartyStatus;
import net.softm.data.kata.util.base.GroupUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FbGodWhisper {
    private static final String TAG = "FbGodWhisper";

    public static FirebaseFirestore instance() {
        return FirebaseFirestore.getInstance();
    }

    static String COLLECTION_PRAY_CLASS = "prayClass"; // prayComment
    static String COLLECTION_PRAY_COMMENT = "prayComment"; // prayComment
    static String COLLECTION_PRAY_MEMO = "prayMemo"; // prayMemo

    public static void createPrayClass(Context context, PrayClass prayClass, ParamCallback paramCallback) {
        prayClass.setUuid(DeviceUtil.getUUID(context));
        prayClass.setwDate(new Timestamp(new Date(System.currentTimeMillis())));
        try {
            /** Firestore에 데이터 추가 */
            instance().collection(COLLECTION_PRAY_CLASS)
                    .add(prayClass)
                    .addOnSuccessListener(documentReference -> {
                        Fa.i(context, TAG, FaGodWhisper.EVENT_CREATE_PRAY_CLASS, documentReference.getId());
                        if (paramCallback != null) {
                            paramCallback.onResult(prayClass);
                        }// prayClass/m6siSiaLFxVlWiw3s1Q4
                    })
                    .addOnFailureListener(e -> {
                        L.e(TAG, FaGodWhisper.EVENT_CREATE_PRAY_CLASS,e);
                        if (paramCallback != null) {
                            paramCallback.onResult(null);
                        }
                    });
        } catch (Exception ex) {
            L.e(TAG, "createPrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS, ex);
            if (paramCallback != null) {
                paramCallback.onResult(null);
            }
        }
    }

    public static void selectPrayClass(Context context, Timestamp fromDt, Timestamp toDt, String title, ParamCallback paramCallback) {
        try {
            String start = title.toLowerCase();
            String end = start + "\uf8ff";
            /** Firestore에 데이터 조회 */
            Query query = instance().collection(COLLECTION_PRAY_CLASS)
                    .whereGreaterThanOrEqualTo("title", start)
                    .whereLessThanOrEqualTo("title", end)
                    .whereGreaterThanOrEqualTo("prayDt", fromDt)
                    .whereLessThanOrEqualTo("prayDt", toDt);

            List<Task<QuerySnapshot>> taskList = new ArrayList<>();

            if (GroupUtil.isGroupMasterUUIDs(OrgUtil.getUuid())) {
                taskList.add(query.whereEqualTo("uuid", DeviceUtil.getUUID(context)).get());
                taskList.add(query.whereArrayContains("partyUuids", OrgUtil.getUuid()).get());
            } else {
                taskList.add(query.whereEqualTo("uuid", DeviceUtil.getUUID(context)).get());
                if ( OrgUtil.getGroups().size() > 0 ) taskList.add(query.whereArrayContainsAny("groups", OrgUtil.getGroups()).get());
                taskList.add(query.whereArrayContains("partyUuids", DeviceUtil.getUUID(context)).get());
            }
            Tasks.whenAllSuccess(taskList)
                    .addOnCompleteListener(tasks -> {
                        if (tasks.isSuccessful()) {
                            Set<PrayClass> dataSet = new HashSet<>();
                            for (Object querySnapshot_ : tasks.getResult()) {

//                        Task<QuerySnapshot> querySnapshotTask = (Task<QuerySnapshot>) task;
                                QuerySnapshot querySnapshot = (QuerySnapshot) querySnapshot_;
//                    for (Task<QuerySnapshot> task : tasks.getResult()) {
//                        if (task instanceof Task<?>) {
//                            Task<QuerySnapshot> querySnapshotTask = (Task<QuerySnapshot>) task;
//                            QuerySnapshot querySnapshot = querySnapshotTask.getResult();
                                if (querySnapshot != null) {
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        String docId = document.getId();
                            /*
                                String uuid = document.getString("uuid");
                                String title_ = document.getString("title");
                                String orgId = document.getString("orgId");
                                Timestamp wDate = document.getTimestamp("wDate");
                                Timestamp uDate = document.getTimestamp("uDate");
                                Timestamp prayDt = document.getTimestamp("prayDt");

                                List<String> groups = (List<String>) document.get("groups");
                                List<String> partyUuids = (List<String>) document.get("partyUuids");
                                Object parties = document.get("parties");

                                PrayClass prayClass = new PrayClass(docId, uuid, title_, prayDt, wDate, uDate, groups, orgId, (Map<String, PrayClassParty>) parties);
                            */
                                        PrayClass prayClass = document.toObject(PrayClass.class);
                                        prayClass.setDocId(docId);
                                        prayClass.gen();

                                        /**
                                         selectPrayClass 메서드에 or 조건으로
                                         uuid가 partyUuids에 존재하거나 또는(or),
                                         uuid가 partyUuids에 없으면서 prdDt가 현재시간시간보다 큰 데이터
                                         prayDt기준 2시간이 지난 자료는 예배가종료됨
                                         */
//                                    if (GroupUtil.isGroupMasterUUIDs(WApplication.basicInfoDTO.uuid) || prayClass.isParty || (!prayClass.isParty && prayClass.getPrayDt().compareTo(Timestamp.now()) >= 0)) {
                                        dataSet.add(prayClass);
//                                    }
                                    }
                                }
//                        }

                            }

                            List<PrayClass> dataList = new ArrayList<>(dataSet);
                            if (paramCallback != null) {
                                paramCallback.onResult(dataList);
                            }
                            L.i( TAG, "selectPrayClass", "success", FaGodWhisper.EVENT_SELECT_PRAY_CLASS);
                        } else {
                            L.e( TAG, "selectPrayClass", "!success", FaGodWhisper.EVENT_SELECT_PRAY_CLASS,tasks.getException());
                            if (paramCallback != null) {
                                paramCallback.onResult(null);
                            }
                        }
                    });
        } catch (Exception ex) {
            L.e( TAG,"selectPrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS,ex);
            if (paramCallback != null) {
                paramCallback.onResult(null);
            }
        }
    }

    public static void selectOnePrayClass(Context context, String docId, ParamCallback paramCallback) {
        try {
            /** Firestore에 데이터 조회 */
            instance().collection(COLLECTION_PRAY_CLASS).document(docId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
//                            QuerySnapshot querySnapshot = task.getResult();
//                            List<PrayClass> dataList = new ArrayList<>();
                            PrayClass prayClass = new PrayClass();
                            if (document != null) {
                                /*
                                    String uuid = document.getString("uuid");
                                    String title_ = document.getString("title");
                                    String orgId = document.getString("orgId");
                                    Timestamp wDate = document.getTimestamp("wDate");
                                    Timestamp uDate = document.getTimestamp("uDate");
                                    Timestamp prayDt = document.getTimestamp("prayDt");
                                    List<String> groups = (List<String>) document.get("groups");
                                    Object parties = document.get("parties");
                                    prayClass = new PrayClass(docId, uuid, title_, prayDt, wDate, uDate, groups, orgId, (Map<String, PrayClassParty>) parties);
                                */
                                prayClass = document.toObject(PrayClass.class);
                                String docId_ = document.getId();
                                prayClass.setDocId(docId_);
                                prayClass.gen();
                            }
                            if (paramCallback != null) {
                                paramCallback.onResult(prayClass);
                            }
                            L.i( TAG, "selectOnePrayClass", "success", FaGodWhisper.EVENT_SELECTONE_PRAY_CLASS);
                        } else {
                            L.e( TAG, "selectOnePrayClass", "!success", FaGodWhisper.EVENT_SELECTONE_PRAY_CLASS);
                            if (paramCallback != null) {
                                paramCallback.onResult(null);
                            }
                        }
                    });
        } catch (Exception ex) {
            L.e( TAG,"selectOnePrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS);
            if (paramCallback != null) {
                paramCallback.onResult(null);
            }
        }
    }

    public static void updatePrayClass(Context context, String docId, PrayClass prayClass, ParamCallback paramCallback) {
        Map<String, Object> document = new HashMap<>();
        document.put("uDate", (new Timestamp(new Date(System.currentTimeMillis()))));
        document.put("title", prayClass.getTitle());
        document.put("content", prayClass.getContent());
//        document.put("prayDt", Fb.toTimestamp(prayDt));
        document.put("groups", prayClass.getGroups());

        try {
            instance().collection(COLLECTION_PRAY_CLASS).document(docId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                if (documentSnapshot.get("uuid").equals(DeviceUtil.getUUID(context))) {
                                    Fa.i(context, TAG, FaGodWhisper.EVENT_UPDATE_PRAY_CLASS, documentSnapshot.getData());
                                    Map<String, Object> updates = new HashMap<>();
                                    documentSnapshot.getReference().update(document);
                                    if (paramCallback != null) {
                                        prayClass.setuDate((Timestamp) document.get("uDate"));
                                        paramCallback.onResult(prayClass);
//                                        paramCallback.onResult((new Gson()).fromJson((new Gson()).toJson(document),PrayClass.class));
                                    }
                                    L.i( TAG, "updatePrayClass", "success", FaGodWhisper.EVENT_UPDATE_PRAY_CLASS);
                                }
                            }
                        } else {
                            L.e( TAG, "updatePrayClass", "!success", FaGodWhisper.EVENT_UPDATE_PRAY_CLASS,task.getException());
                            if (paramCallback != null) {
                                paramCallback.onResult(null);
                            }
                        }
                    });
        } catch (Exception ex) {
            L.e( TAG, "updatePrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS, ex);
            if (paramCallback != null) {
                paramCallback.onResult(null);
            }
        }
    }

    public static void deletePrayClass(Context context, String docId, ParamCallback paramCallback) {
        Map<String, Object> document = new HashMap<>();
        try {
            instance().collection(COLLECTION_PRAY_CLASS).document(docId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                if (documentSnapshot.get("uuid").equals(DeviceUtil.getUUID(context))) {
                                    Fa.i(context, TAG, FaGodWhisper.EVENT_DELETE_PRAY_CLASS, documentSnapshot.getData());
                                    Map<String, Object> updates = new HashMap<>();
                                    documentSnapshot.getReference().delete();
                                    if (paramCallback != null) {
                                        paramCallback.onResult("ok");
                                    }
                                    L.i( TAG, "deletePrayClass", "success", FaGodWhisper.EVENT_DELETE_PRAY_CLASS);
                                }
                            }
                        } else {
                            if (paramCallback != null) {
                                paramCallback.onResult("error");
                            }
                            L.e( TAG, "deletePrayClass", "!success", FaGodWhisper.EVENT_DELETE_PRAY_CLASS,task.getException());
                        }
                    });
        } catch (Exception ex) {
            L.e( TAG, "deletePrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS, ex);
            if (paramCallback != null) {
                paramCallback.onResult("error");
            }
        }

    }

    public static void addPartyPrayClass(Context context, String docId, ParamCallback callback) {
        PrayClassParty prayClassParty = new PrayClassParty();
        prayClassParty.setUuid(OrgUtil.getUuid());
        prayClassParty.setwDate((new Timestamp(new Date(System.currentTimeMillis()))));
        try {
            instance().collection(COLLECTION_PRAY_CLASS).document(docId).get()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot documentSnapshot = task.getResult();
//                            if (documentSnapshot != null && documentSnapshot.exists()) {
//                                Fa.i(context, TAG, FaGodWhisper.EVENT_ADD_PARTY_PRAY_CLASS, documentSnapshot.getData());
//                                Map<String, Object> updates = new HashMap<>();
//                                documentSnapshot.getReference().update("parties."+WApplication.basicInfoDTO.uuid, party);
//                                if (callback != null) {
//                                    callback.onResult("ok");
//                                }
//                            }
//                        } else {
//                            if (callback != null) {
//                                callback.onResult("error");
//                            }
//                            L.e(TAG, "Error PrayClass not exists : " + task.getException());
//                        }

//                    })
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String uuid = OrgUtil.getUuid();
                            documentSnapshot.getReference()
                                 // .update("parties." + uuid, party, "wDate", FieldValue.serverTimestamp(),
                                    .update("parties." + uuid, prayClassParty, "wDate", FieldValue.serverTimestamp(),
                                            "partyUuids", FieldValue.arrayUnion(uuid))
                                    .addOnSuccessListener(aVoid -> {
                                        Fa.i( context, TAG, FaGodWhisper.EVENT_ADD_PARTY_PRAY_CLASS);
                                        callback.onResult("ok");
                                    });
                        } else {
                            callback.onResult("error");
                            L.e( TAG, "addPartyPrayClass", FaGodWhisper.EVENT_ADD_PARTY_PRAY_CLASS);
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onResult("error");
                        L.e( TAG, "addPartyPrayClass", FaGodWhisper.EVENT_ADD_PARTY_PRAY_CLASS, e);
                    });

        } catch (Exception ex) {
            if (callback != null) {
                callback.onResult("error");
            }
            L.e( TAG, "addPartyPrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS, ex);
        }
    }

    public static void removePartyPrayClass(Context context, String docId, ParamCallback callback) {
        String uuid = OrgUtil.getUuid();
        try {
            instance().collection(COLLECTION_PRAY_CLASS).document(docId)
                    .update("parties." + uuid, FieldValue.delete(),
                            "partyUuids", FieldValue.arrayRemove(uuid))
                    .addOnSuccessListener(aVoid -> {
                        Fa.i( context, TAG, FaGodWhisper.EVENT_REMOVE_PARTY_PRAY_CLASS);
                        callback.onResult("ok");
                    })
                    .addOnFailureListener(e -> {
                        callback.onResult("error");
                        L.e( TAG, "removePartyPrayClass", FaGodWhisper.EVENT_REMOVE_PARTY_PRAY_CLASS, e);
                    });
        } catch (Exception ex) {
            if (callback != null) {
                callback.onResult("error");
            }
            L.e( TAG, "removePartyPrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS, ex);
        }
    }

    public static void donePartyPrayClass(Context context, String docId, ParamCallback callback) {
        try {
            instance().collection(COLLECTION_PRAY_CLASS).document(docId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String uuid = OrgUtil.getUuid();
                            PrayClass prayClass = documentSnapshot.toObject(PrayClass.class);
                            Map<String, PrayClassParty> parties = prayClass.getParties();
                            PrayClassParty prayClassParty = null;
                            if ( parties != null ) {
                                prayClassParty = parties.get(uuid);
                                prayClassParty.setPrayPartyStatus(PrayPartyStatus.DOPARTY);
                                prayClassParty.setuDate(new Timestamp(new Date(System.currentTimeMillis())));
                            }
                            if ( prayClassParty != null ) {
                                documentSnapshot.getReference()
                                        .update("parties." + uuid, prayClassParty)
                                        .addOnSuccessListener(aVoid -> {
                                            callback.onResult("ok");
                                            Fa.i(context, TAG, FaGodWhisper.EVENT_DONE_PARTY_PRAY_CLASS);
                                        });
                            } else {
                                callback.onResult("error");
                            }
                        } else {
                            callback.onResult("error");
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onResult("error");
                        L.e( TAG, "donePartyPrayClass", FaGodWhisper.EVENT_DONE_PARTY_PRAY_CLASS, e);
                    });

        } catch (Exception ex) {
            if (callback != null) {
                callback.onResult("error");
            }
            L.e( TAG, "donePartyPrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS, ex);
        }
    }

    public static void notPartyPrayClass(Context context, String docId, ParamCallback callback) {
        try {
            instance().collection(COLLECTION_PRAY_CLASS).document(docId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String uuid = OrgUtil.getUuid();
                            PrayClass prayClass = documentSnapshot.toObject(PrayClass.class);
                            Map<String, PrayClassParty> parties = prayClass.getParties();
                            PrayClassParty prayClassParty = null;
                            if ( parties != null ) {
                                prayClassParty = parties.get(uuid);
                                prayClassParty.setPrayPartyStatus(PrayPartyStatus.NOTPARTY);
                                prayClassParty.setuDate(new Timestamp(new Date(System.currentTimeMillis())));
                            }
                            if ( prayClassParty != null ) {
                                documentSnapshot.getReference()
                                        .update("parties." + uuid, prayClassParty)
                                        .addOnSuccessListener(aVoid -> {
                                            callback.onResult("ok");
                                            Fa.i(context, TAG, FaGodWhisper.EVENT_DONE_PARTY_PRAY_CLASS);
                                        });
                            } else {
                                callback.onResult("error");
                            }
                        } else {
                            callback.onResult("error");
                            L.e(  TAG,  "notPartyPrayClass", FaGodWhisper.EVENT_NOT_PARTY_PRAY_CLASS);
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onResult("error");
                        L.e(  TAG,  "notPartyPrayClass", FaGodWhisper.EVENT_NOT_PARTY_PRAY_CLASS, e);
                    });
        } catch (Exception ex) {
            if (callback != null) {
                callback.onResult("error");
            }
            L.e( TAG, "notPartyPrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS, ex);
        }
    }


    public static void selectTodayToOneDayPrayClass(Context context, Timestamp fromDt, Timestamp toDt, ParamCallback paramCallback) {
        try {
//            String start = title.toLowerCase();
//            String end = start + "\uf8ff";
            /** Firestore에 데이터 조회 */
            Query query = instance().collection(COLLECTION_PRAY_CLASS)
                    .whereGreaterThanOrEqualTo("prayDt", fromDt)
                    .whereLessThanOrEqualTo("prayDt", toDt)
                    .orderBy("prayDt", Query.Direction.ASCENDING)
            ;

            List<Task<QuerySnapshot>> taskList = new ArrayList<>();

            if (GroupUtil.isGroupMasterUUIDs(WApplication.basicInfoDTO.uuid)) { /** 그룹 마스터 */
                taskList.add(query.whereEqualTo("uuid", DeviceUtil.getUUID(context)).get());
                taskList.add(query.whereArrayContains("partyUuids", DeviceUtil.getUUID(context)).get());
            } else { /** 일반회원 */
                taskList.add(query.whereEqualTo("uuid", DeviceUtil.getUUID(context)).get());
                if ( OrgUtil.getGroups().size() > 0 ) taskList.add(query.whereArrayContainsAny("groups", OrgUtil.getGroups()).get());
                taskList.add(query.whereArrayContains("partyUuids", DeviceUtil.getUUID(context)).get());
            }
//            query.whereGreaterThanOrEqualTo("title", start)  // "title"이 시작하는 부분
//                    .whereLessThanOrEqualTo("title", end)
            Tasks.whenAllSuccess(taskList)
                    .addOnCompleteListener(tasks -> {
                        if (tasks.isSuccessful()) {
                            Set<PrayClass> dataSet = new HashSet<>();
                            for (Object querySnapshot_ : tasks.getResult()) {
                                QuerySnapshot querySnapshot = (QuerySnapshot) querySnapshot_;
                                if (querySnapshot != null) {
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        String docId = document.getId();
                            /*
                                    String uuid = document.getString("uuid");
                                    String title_ = document.getString("title");
                                    String orgId = document.getString("orgId");
                                    Timestamp wDate = document.getTimestamp("wDate");
                                    Timestamp uDate = document.getTimestamp("uDate");
                                    Timestamp prayDt = document.getTimestamp("prayDt");

                                    List<String> groups = (List<String>) document.get("groups");
                                    Object parties = document.get("parties");
                                    PrayClass prayClass = new PrayClass(docId, uuid, title_, prayDt, wDate, uDate, groups, orgId, (Map<String, PrayClassParty>) parties);
                            */
                                        PrayClass prayClass = document.toObject(PrayClass.class);
                                        prayClass.setDocId(docId);
                                        prayClass.gen();
                                        dataSet.add(prayClass);
                                    }
                                }
//                        }
                            }

                            List<PrayClass> dataList = new ArrayList<>(dataSet);
                            if (paramCallback != null) {
                                paramCallback.onResult(dataList);
                            }
                            L.i( TAG, "selectTodayToOneDayPrayClass", "success", FaGodWhisper.EVENT_SELECT_PRAY_CLASS);
                        } else {
                            L.e( TAG, "selectTodayToOneDayPrayClass", "!success", FaGodWhisper.EVENT_SELECT_PRAY_CLASS, tasks.getException());
                            if (paramCallback != null) {
                                paramCallback.onResult(new ArrayList<>());
                            }
                        }
                    });
        } catch (Exception ex) {
            L.e( TAG, "selectTodayToOneDayPrayClass", FaGodWhisper.EVENT_ERROR_PRAY_CLASS, ex);
            if (paramCallback != null) {
                paramCallback.onResult(new ArrayList<>());
            }
        }
    }

    public static void addPrayComment(Context context, String classDocId, PrayComment prayComment, ParamCallback paramCallback) {
        prayComment.setClassDocId(classDocId);
        prayComment.setUuid(OrgUtil.getUuid());
        prayComment.setwDate(new Timestamp(new Date(System.currentTimeMillis())));

        // Firestore 배치 작업 생성
        WriteBatch batch = instance().batch();
        try {
            // 1. 댓글 정보를 PrayClass 컬렉션에 먼저 추가하는 작업
            instance().collection(COLLECTION_PRAY_COMMENT).add(prayComment)
                    .addOnSuccessListener(documentReference -> {
                        Fa.i(context, TAG, FaGodWhisper.EVENT_ADD_PRAY_COMMENT, documentReference.getId());

                        // 댓글이 성공적으로 추가된 후 commentDocId를 PrayClass 문서에 추가
                        String commentDocId = documentReference.getId();

                        // 2. "PrayClass" 문서에 commentDocIds 배열에 추가하는 작업
                        instance().collection(COLLECTION_PRAY_CLASS).document(classDocId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        // 배치에 "commentDocIds" 배열에 commentDocId 추가 작업 추가
                                        batch.update(documentSnapshot.getReference(), "commentDocIds", FieldValue.arrayUnion(commentDocId));

                                        // 배치에서 모든 작업을 커밋
                                        batch.commit()
                                                .addOnSuccessListener(aVoid -> {
                                                    // 성공적으로 배치 작업이 완료된 후 콜백 호출
                                                    if (paramCallback != null) {
                                                        paramCallback.onResult(prayComment);
                                                    }
                                                    Fa.i(context, TAG, FaGodWhisper.EVENT_ADD_PRAY_COMMENT);
                                                })
                                                .addOnFailureListener(e -> {
                                                    L.e( TAG, "addPrayComment", FaGodWhisper.EVENT_ADD_PRAY_COMMENT, e);
                                                    if (paramCallback != null) {
                                                        paramCallback.onResult(null);
                                                    }
                                                });
                                    } else {
                                        L.e( TAG, "addPrayComment", FaGodWhisper.EVENT_ADD_PRAY_COMMENT,"not-exists");
                                        // 문서가 없을 경우 에러 처리
                                        paramCallback.onResult(null);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    L.e( TAG, "addPrayComment", FaGodWhisper.EVENT_ADD_PRAY_COMMENT, e);
                                    if (paramCallback != null) {
                                        paramCallback.onResult(null);
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Fc.e(context, TAG, "addPrayComment", FaGodWhisper.EVENT_ERROR_PRAY_COMMENT, e);
                        if (paramCallback != null) {
                            paramCallback.onResult(null);
                        }
                    });
        } catch (Exception ex) {
            Fc.e(context, TAG, "addPrayComment", FaGodWhisper.EVENT_ERROR_PRAY_CLASS, ex);
            if (paramCallback != null) {
                paramCallback.onResult(null);
            }
        }
    }

    public static void selectPrayComment(Context context, PrayComment prayComment, ParamCallback paramCallback) {
        try {
            /** Firestore에 데이터 조회 */
            Query query = instance().collection(COLLECTION_PRAY_COMMENT);
            List<Task<QuerySnapshot>> taskList = new ArrayList<>();
            taskList.add(
                query.whereEqualTo("classDocId", prayComment.getClassDocId()).orderBy("wDate", Query.Direction.DESCENDING).get()
            );

            Tasks.whenAllSuccess(taskList)
            .addOnCompleteListener(tasks -> {
                if (tasks.isSuccessful()) {
                    Set<PrayComment> dataSet = new HashSet<>();
                    for (Object querySnapshot_ : tasks.getResult()) {

                        QuerySnapshot querySnapshot = (QuerySnapshot) querySnapshot_;
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String docId = document.getId();
                                PrayComment prayComment_ = document.toObject(PrayComment.class);
                                prayComment_.setDocId(docId);
                                prayComment_.gen();

                                dataSet.add(prayComment_);
                            }
                        }
                    }

                    List<PrayComment> dataList = new ArrayList<>(dataSet);
                    if (paramCallback != null) {
                        paramCallback.onResult(dataList);
                    }
                    L.i( TAG, "selectPrayComment", "success", FaGodWhisper.EVENT_SELECT_PRAY_COMMENT);
                } else {
                    if (paramCallback != null) {
                        paramCallback.onResult(null);
                    }
                    L.e( TAG, "selectPrayComment", "!success", FaGodWhisper.EVENT_SELECT_PRAY_COMMENT, tasks.getException());
                }
            });
        } catch (Exception ex) {
            L.e( TAG, "selectPrayComment", FaGodWhisper.EVENT_ERROR_PRAY_COMMENT, ex);
            if (paramCallback != null) {
                paramCallback.onResult(null);
            }
        }
    }

    public static void deletePrayComment(Context context, PrayComment prayComment, ParamCallback paramCallback) {
        String docId = prayComment.getDocId();
        String classDocId = prayComment.getClassDocId();

        WriteBatch batch = instance().batch();

        try {
            // 1. PrayClass 문서에서 commentDocIds 배열에서 댓글 ID 제거
            instance().collection(COLLECTION_PRAY_CLASS).document(classDocId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            // 상위 문서가 존재하지 않으면 실패 처리
                            if (paramCallback != null) paramCallback.onResult(null);
                            return;
                        }

                        // commentDocIds 배열에서 댓글 ID 제거
                        batch.update(documentSnapshot.getReference(), "commentDocIds", FieldValue.arrayRemove(docId));

                        // 2. 댓글 문서 삭제 (docId 기준)
                        DocumentReference commentDocRef = instance().collection(COLLECTION_PRAY_COMMENT).document(docId);
                        batch.delete(commentDocRef);

                        // 3. 배치 커밋
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Fa.i(context, TAG, FaGodWhisper.EVENT_DELETE_PRAY_COMMENT);
                                    if (paramCallback != null) {
                                        paramCallback.onResult("ok");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    L.e(TAG, FaGodWhisper.EVENT_DELETE_PRAY_COMMENT, "deletePrayComment", e);
                                    if (paramCallback != null) {
                                        paramCallback.onResult(null);
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        L.e(TAG, FaGodWhisper.EVENT_DELETE_PRAY_COMMENT, "deletePrayComment", e);
                        if (paramCallback != null) {
                            paramCallback.onResult(null);
                        }
                    });

        } catch (Exception ex) {
            L.e(TAG, FaGodWhisper.EVENT_DELETE_PRAY_COMMENT, "deletePrayComment", ex);
            if (paramCallback != null) {
                paramCallback.onResult(null);
            }
        }
    }
    public static void savePrayMemo(Context context, Timestamp timestampId, PrayMemo prayMemo, ParamCallback paramCallback) {
        prayMemo.setTimestampId(timestampId);
        prayMemo.setUuid(OrgUtil.getUuid());
        try {
            // Firestore query to check if document with timestampId exists
            Query query = instance().collection(COLLECTION_PRAY_MEMO)
                    .whereEqualTo("uuid", OrgUtil.getUuid())
                    .whereEqualTo("timestampId", timestampId)
                    .limit(1);

            Fa.i(context, TAG, FaGodWhisper.EVENT_UPDATE_PRAY_MEMO, "start~");

            query.get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Document exists, update the existing document
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            DocumentReference docRef = documentSnapshot.getReference();

                            // Update the existing document with the new data
                            docRef.update("uDate", new Timestamp(new Date(System.currentTimeMillis())),
                                           "memo", prayMemo.getMemo()
                                    )
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully updated the document, return the updated PrayMemo
                                        prayMemo.setDocId(documentSnapshot.getId());
                                        if (paramCallback != null) {
                                            paramCallback.onResult(prayMemo);  // Pass the updated PrayMemo
                                        }
                                        Fa.i(context, TAG, FaGodWhisper.EVENT_UPDATE_PRAY_MEMO, prayMemo.getDocId());
                                    })
                                    .addOnFailureListener(e -> {
                                        L.e(TAG, "savePrayMemo", FaGodWhisper.EVENT_ERROR_PRAY_MEMO, e);
                                        if (paramCallback != null) {
                                            paramCallback.onResult(null);
                                        }
                                    });
                        } else {
                            prayMemo.setwDate(new Timestamp(new Date(System.currentTimeMillis())));
                            // Document doesn't exist, create a new one
                            instance().collection(COLLECTION_PRAY_MEMO)
                                    .add(prayMemo)
                                    .addOnSuccessListener(documentReference -> {
                                        Fa.i(context, TAG, FaGodWhisper.EVENT_ADD_PRAY_MEMO, documentReference.getId());
                                        prayMemo.setDocId(documentReference.getId());
                                        if (paramCallback != null) {
                                            paramCallback.onResult(prayMemo);  // Pass the created PrayMemo
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        L.e(TAG, "savePrayMemo", FaGodWhisper.EVENT_ERROR_PRAY_MEMO, e);
                                        if (paramCallback != null) {
                                            paramCallback.onResult(null);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Fc.e(context, TAG, "savePrayMemo", FaGodWhisper.EVENT_ERROR_PRAY_MEMO, e);
                        if (paramCallback != null) {
                            paramCallback.onResult(null);
                        }
                    });
        } catch (Exception ex) {
            Fc.e(context, TAG, "savePrayMemo", FaGodWhisper.EVENT_ERROR_PRAY_MEMO, ex);
            if (paramCallback != null) {
                paramCallback.onResult(null);
            }
        }
    }


    public static void selectPrayMemo(Context context, List<Timestamp> timestampIds, ParamCallback paramCallback) {
        try {
            /** Firestore 데이터 조회 */
            Query query = instance().collection(COLLECTION_PRAY_MEMO);

            // Dynamically create the timestampIds list from the getTimestamp() method of each PrayMemo
//            List<Timestamp> timestampIds = new ArrayList<>();
//
//            for (PrayMemo prayMemo : prayMemos) {
//                Timestamp timestamp = prayMemo.getTimestampId();  // Assuming getTimestamp() returns a Timestamp
//                if (timestamp != null) {
//                    timestampIds.add(timestamp);
//                }
//            }

            // Check if timestampIds is not empty before proceeding
            if (timestampIds.isEmpty()) {
                if (paramCallback != null) {
                    paramCallback.onResult(null);
                }
                return;  // Return early if the list is empty
            }

            // Apply the filter and ordering
            List<Task<QuerySnapshot>> taskList = new ArrayList<>();
            taskList.add(
                query.whereIn("timestampId", timestampIds)  // Pass the dynamically created list of Timestamp objects
                     .whereEqualTo("uuid", OrgUtil.getUuid())
                     .orderBy("wDate", Query.Direction.DESCENDING)
                     .get()
            );
            // Execute all tasks when completed
            Tasks.whenAllSuccess(taskList)
                    .addOnCompleteListener(tasks -> {
                        if (tasks.isSuccessful()) {
                            Set<PrayMemo> dataSet = new HashSet<>();
                            for (Object querySnapshot_ : tasks.getResult()) {

                                QuerySnapshot querySnapshot = (QuerySnapshot) querySnapshot_;
                                if (querySnapshot != null) {
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        String docId = document.getId();
                                        PrayMemo prayMemo_ = document.toObject(PrayMemo.class);
                                        prayMemo_.setDocId(docId);
                                        prayMemo_.gen();  // Assuming this is some data processing method

                                        dataSet.add(prayMemo_);
                                    }
                                }
                            }

                            // Convert the Set to a List and return the result via callback
                            List<PrayMemo> dataList = new ArrayList<>(dataSet);
                            if (paramCallback != null) {
                                paramCallback.onResult(dataList);
                            }

                            // Log the success
                            L.i(TAG, "selectPrayMemo", "success", FaGodWhisper.EVENT_SELECT_PRAY_MEMO);
                        } else {
                            // Handle task failure
                            if (paramCallback != null) {
                                paramCallback.onResult(null);
                            }

                            // Log the error
                            L.e(TAG, "selectPrayMemo", "!success", FaGodWhisper.EVENT_SELECT_PRAY_MEMO, tasks.getException());
                        }
                    });
        } catch (Exception ex) {
            // Handle any unexpected exceptions
            L.e(TAG, "selectPrayMemo", FaGodWhisper.EVENT_ERROR_PRAY_MEMO, ex);
            if (paramCallback != null) {
                paramCallback.onResult(null);
            }
        }
    }



    // ✅ 4. 전체 파티 목록 가져오기
    public static void getPartyList(String docId, ParamCallback callback) {
        instance().collection(COLLECTION_PRAY_CLASS).document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> parties = (Map<String, Object>) documentSnapshot.get("parties");
                        callback.onResult(parties);
                    } else {
                        callback.onResult(null);
                    }
                });
    }

    // ✅ 5. regDt 기준 정렬된 파티 목록 반환
    public static void getSortedPartyList(String docId, ParamCallback callback) {
        instance().collection(COLLECTION_PRAY_CLASS).document(docId).get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> parties = (Map<String, Object>) doc.get("parties");
                    if (parties != null) {
                        List<Map<String, Object>> sorted = new ArrayList<>();
                        for (Object val : parties.values()) {
                            if (val instanceof Map) {
                                sorted.add((Map<String, Object>) val);
                            }
                        }
                        sorted.sort(Comparator.comparing(m -> ((Timestamp) m.get("regDt")).toDate()));
                        callback.onResult(sorted);
                    } else {
                        callback.onResult(Collections.emptyList());
                    }
                });
    }

}