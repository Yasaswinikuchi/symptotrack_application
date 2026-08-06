package com.symtotrack

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

class FamilyActivity : BaseActivity() {

    private lateinit var containerList: LinearLayout
    private val familyArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        containerList = findViewById(R.id.layout_family_list)

        loadSavedFamilyMembers()

        findViewById<Button>(R.id.btn_add_member).setOnClickListener {
            showAddEditFamilyMemberDialog(null, -1)
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────
    private fun loadSavedFamilyMembers() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("family_members_list", null)

        // Clear existing
        while (familyArray.length() > 0) familyArray.remove(0)

        if (!raw.isNullOrEmpty()) {
            try {
                val saved = JSONArray(raw)
                for (i in 0 until saved.length()) familyArray.put(saved.getJSONObject(i))
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            // Default sample members
            familyArray.put(JSONObject().apply {
                put("name", "Sarah Jenkins"); put("age", "26"); put("gender", "Female")
                put("relationship", "Spouse"); put("blood", "A+"); put("conditions", "None")
            })
            familyArray.put(JSONObject().apply {
                put("name", "Leo Jenkins"); put("age", "4"); put("gender", "Male")
                put("relationship", "Child"); put("blood", "O+"); put("conditions", "Mild Seasonal Allergy")
            })
            saveFamilyMembers()
        }

        renderFamilyMembers()
    }

    private fun saveFamilyMembers() {
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit().putString("family_members_list", familyArray.toString()).apply()
    }

    // ── Render ────────────────────────────────────────────────────────────────
    private fun renderFamilyMembers() {
        containerList.removeAllViews()

        // ── "Self (Primary)" card from logged-in user profile ────────────────
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val selfName = prefs.getString("user_name", "Me") ?: "Me"
        val selfAge  = prefs.getString("user_age", "—") ?: "—"
        val selfGender = prefs.getString("user_gender", "—") ?: "—"
        val selfBlood  = prefs.getString("user_blood", "—") ?: "—"
        val selfCond   = prefs.getString("user_conditions", "None") ?: "None"

        val selfItem = JSONObject().apply {
            put("name", selfName); put("age", selfAge); put("gender", selfGender)
            put("relationship", "Self"); put("blood", selfBlood); put("conditions", selfCond)
            put("isSelf", true)
        }

        addCardView(selfItem, -1)   // -1 = self profile (not in familyArray)

        // ── Family members ────────────────────────────────────────────────────
        for (i in 0 until familyArray.length()) {
            addCardView(familyArray.getJSONObject(i), i)
        }
    }

    private fun addCardView(item: JSONObject, index: Int) {
        val name = item.optString("name", "Family Member")
        val age  = item.optString("age", "")
        val gender = item.optString("gender", "")
        val rel    = item.optString("relationship", "")
        val blood  = item.optString("blood", "")
        val cond   = item.optString("conditions", "")
        val isSelf = item.optBoolean("isSelf", false)

        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_family_card, containerList, false)

        view.findViewById<TextView>(R.id.tv_member_name).text =
            if (rel.isNotEmpty()) "$name ($rel)" else name

        view.findViewById<TextView>(R.id.tv_member_details).text =
            "Age: ${age.ifEmpty { "—" }} • ${gender.ifEmpty { "—" }} • Blood: ${blood.ifEmpty { "—" }}"

        val tvCond = view.findViewById<TextView>(R.id.tv_member_conditions)
        if (cond.isNotEmpty() && cond.lowercase() != "none") {
            tvCond.visibility = View.VISIBLE
            tvCond.text = "⚠ Conditions: $cond"
        } else {
            tvCond.visibility = View.GONE
        }

        // Edit action – both the card AND the edit icon open the dialog
        val editAction = View.OnClickListener {
            if (isSelf) showEditSelfDialog() else showAddEditFamilyMemberDialog(item, index)
        }

        view.setOnClickListener(editAction)
        view.findViewById<ImageButton>(R.id.btn_edit_member).setOnClickListener(editAction)

        // Delete – not allowed for self
        val btnDelete = view.findViewById<ImageButton>(R.id.btn_delete_member)
        if (isSelf) {
            btnDelete.visibility = View.GONE
        } else {
            btnDelete.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Remove Member")
                    .setMessage("Remove $name from family health profiles?")
                    .setPositiveButton("Remove") { _, _ ->
                        familyArray.remove(index)
                        saveFamilyMembers()
                        renderFamilyMembers()
                        Toast.makeText(this, "$name removed.", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        containerList.addView(view)
    }

    // ── Edit "Self" (logged-in user) ──────────────────────────────────────────
    private fun showEditSelfDialog() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        fun makeField(hint: String, current: String): EditText {
            return EditText(this).apply {
                this.hint = hint
                setText(current)
                setPadding(0, 8, 0, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 16 }
            }
        }

        val etName   = makeField("Full Name", prefs.getString("user_name","") ?: "")
        val etAge    = makeField("Age", prefs.getString("user_age","") ?: "")
        val etGender = makeField("Gender (Male/Female/Other)", prefs.getString("user_gender","") ?: "")
        val etBlood  = makeField("Blood Type (e.g. A+, O-)", prefs.getString("user_blood","") ?: "")
        val etCond   = makeField("Pre-existing Conditions", prefs.getString("user_conditions","None") ?: "None")

        layout.addView(etName); layout.addView(etAge)
        layout.addView(etGender); layout.addView(etBlood); layout.addView(etCond)

        AlertDialog.Builder(this)
            .setTitle("Edit My Profile")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                prefs.edit()
                    .putString("user_name", name)
                    .putString("user_age", etAge.text.toString().trim())
                    .putString("user_gender", etGender.text.toString().trim())
                    .putString("user_blood", etBlood.text.toString().trim())
                    .putString("user_conditions", etCond.text.toString().trim().ifEmpty { "None" })
                    .apply()
                renderFamilyMembers()
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Add / Edit family member dialog ───────────────────────────────────────
    private fun showAddEditFamilyMemberDialog(existingItem: JSONObject?, editIndex: Int) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_family_member, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle      = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val etName       = dialogView.findViewById<EditText>(R.id.et_member_name)
        val etAge        = dialogView.findViewById<EditText>(R.id.et_member_age)
        val etCond       = dialogView.findViewById<EditText>(R.id.et_member_conditions)
        val spGender     = dialogView.findViewById<Spinner>(R.id.spinner_member_gender)
        val spRel        = dialogView.findViewById<Spinner>(R.id.spinner_member_rel)
        val spBlood      = dialogView.findViewById<Spinner>(R.id.spinner_member_blood)

        val genderOpts = arrayOf("Female", "Male", "Other")
        val relOpts    = arrayOf("Spouse", "Child", "Parent", "Sibling", "Other")
        val bloodOpts  = arrayOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

        spGender.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOpts)
        spRel.adapter    = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, relOpts)
        spBlood.adapter  = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bloodOpts)

        if (existingItem != null) {
            tvTitle.text = "Edit Family Member"
            etName.setText(existingItem.optString("name"))
            etAge.setText(existingItem.optString("age"))
            etCond.setText(existingItem.optString("conditions"))
            val gi = genderOpts.indexOf(existingItem.optString("gender")); if (gi >= 0) spGender.setSelection(gi)
            val ri = relOpts.indexOf(existingItem.optString("relationship")); if (ri >= 0) spRel.setSelection(ri)
            val bi = bloodOpts.indexOf(existingItem.optString("blood")); if (bi >= 0) spBlood.setSelection(bi)
        }

        dialogView.findViewById<Button>(R.id.btn_cancel_member).setOnClickListener { dialog.dismiss() }

        dialogView.findViewById<Button>(R.id.btn_save_member).setOnClickListener {
            val name = etName.text.toString().trim()
            val age  = etAge.text.toString().trim()
            if (name.isEmpty()) { etName.error = "Name required"; return@setOnClickListener }
            if (age.isEmpty())  { etAge.error  = "Age required";  return@setOnClickListener }

            val obj = JSONObject().apply {
                put("name", name); put("age", age)
                put("gender", spGender.selectedItem.toString())
                put("relationship", spRel.selectedItem.toString())
                put("blood", spBlood.selectedItem.toString())
                put("conditions", etCond.text.toString().trim().ifEmpty { "None" })
            }

            if (editIndex >= 0) familyArray.put(editIndex, obj) else familyArray.put(obj)
            saveFamilyMembers()
            renderFamilyMembers()
            dialog.dismiss()
            Toast.makeText(this, "$name saved!", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }
}
