/*
 * Copyright (c) 2022. farrusco (jos dot farrusco at gmail dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.farrusco.filecatalogue.order

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.viewpager2.widget.ViewPager2
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.*
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SearchFiles
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableOrder
import com.farrusco.filecatalogue.tables.TableOrderLine
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.MAIL_ID
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.PRINT_ID
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.SAVE_ID
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.TCPID_ID
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.WHATSAPP_ID
import com.farrusco.projectclasses.databases.XmlBuilder
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueDouble
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.graphics.BitmapManager
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.*
import com.farrusco.projectclasses.utils.CalcObjects.stringToInteger
import com.farrusco.projectclasses.widget.*
import com.farrusco.projectclasses.widget.tablayout.TabLayoutExt
import com.farrusco.projectclasses.widget.tablayout.TabsPagerAdapterExt
import com.farrusco.projectclasses.widget.wheelpicker.BaseWheelPickerView
import com.farrusco.projectclasses.widget.wheelpicker.TextWheelAdapter
import com.farrusco.projectclasses.widget.wheelpicker.TextWheelPickerView
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*
import androidx.core.net.toUri

class DetailOrderTab : BaseActivityTableLayout() {

    //region region vars
    override val layoutResourceId: Int = R.layout.detail_order_tab
    override val mainViewId: Int = R.id.viewMain
    private var listMenu0: Int = 0
    private lateinit var mViewMain0: ScrollView
    private lateinit var mOrderDatum: TextViewExt
    private lateinit var mAfleverDatum: TextViewExt
    private lateinit var mDescription: EditTextExt
    private lateinit var mId: TextViewExt
    private lateinit var mNaam: EditTextExt
    private lateinit var mAddress: EditTextExt
    private lateinit var mEMail: EditTextExt
    private lateinit var mBedrag: TextViewMoney
    private lateinit var mDiscount: EditTextMoney
    private lateinit var pickerView: TextWheelPickerView
    private var bAddressPick = false
    private var orderId: Int = 0
    private var buyerId: Int = 0
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mode = 0
    private lateinit var systeem: Systeem
    private lateinit var order: Order
    private lateinit var orderline: OrderLine
    private lateinit var product: Product
    private var messDialog: Mess? = null
    private lateinit var mTabsViewpager: ViewPager2
    private lateinit var mTabLayout: TabLayoutExt
    private var mTblLayout1: TableLayout? = null

    // private TableRow mRowOrders;
    private lateinit var mTblOrderLines: TableLayout
    private lateinit var mColShort: TextViewExt
    private lateinit var mImage: ImageViewExt
    private var arrId: ArrayList<String>? = null

    private lateinit var mCode: TextViewExt
    private lateinit var mTitle: TextViewExt
    private lateinit var mOms: TextViewExt
    private lateinit var mSubTot: TextViewMoney
    private lateinit var mPrijs: TextViewMoney
    private lateinit var mImgPhoto: ImageViewExt
    private lateinit var btnPhoto: ButtonExt

    // private NumberPicker mNumberPicker;
    private val simpleAdapter = TextWheelAdapter()
    private var orderlineId: Int = -1
    private var orderlineSeqno: Int = -1
    private var orderlineSeqnoCurr: Int = -1
    private var mPosition = 0
    private var objects = StringBuffer()
    //endregion

    //region region init
    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        order = Order(this)
        order = Order(this)
        product = Product(this)
        systeem = Systeem(this)
        orderline = OrderLine()

        messDialog = Mess(this, getText(R.string.mess027_please_wait).toString())

        mTabLayout = findViewById(R.id.tab_layout)
        mTabsViewpager = findViewById(R.id.tabs_viewpager)

        orderId = stringToInteger(intent.getStringExtra(TableOrder.Columns._id.name),0)

        initTabs()
    }

    override fun initTableLayout() {
        // add current activity to enable menu's
        // override: edit_id, delete_id
        if (ConstantsLocal.isPriceUseEnabled) {
            addTableLayout(
                R.id.tblLayout,
                R.layout.detail_order_tab_frag1a,
                orderline,
                arrayOf(
                    TableOrderLine.Columns._productid.name,
                    TableProduct.Columns.title.name,
                    TableOrderLine.Columns.amount.name,
                    TableProduct.Columns.price.name
                ),
                arrayListOf( SAVE_ID, ConstantsFixed.ADD_ID, ConstantsFixed.EDIT_ID, ConstantsFixed.DELETE_ID
                ),
                DetailOrderTab::class.java
            )
        } else {
                addTableLayout(
                    R.id.tblLayout,
                    R.layout.detail_order_tab_frag1b,
                    orderline,
                    arrayOf(
                        TableOrderLine.Columns._productid.name,
                        TableProduct.Columns.title.name,
                        TableOrderLine.Columns.amount.name
                    ),
                    arrayListOf( SAVE_ID, ConstantsFixed.ADD_ID, ConstantsFixed.EDIT_ID, ConstantsFixed.DELETE_ID),
                    DetailOrderTab::class.java  )
        }
    }

    private fun initTabs() {

        val adapter =
            TabsPagerAdapterExt(this, Constants.localFragmentManager!!, Constants.localLifecycle!! )
        adapter.isUserInputEnabled = false
        adapter.setResources(mTabLayout, mTabsViewpager
            ,arrayListOf( R.layout.detail_order_tab_frag0, R.layout.detail_order_tab_frag1 , R.layout.detail_order_tab_frag2 )
            ,arrayListOf( getText(R.string.order).toString(), getText(R.string.lines).toString(), getText(R.string.details).toString() ))

        mTabLayout.setOnTabClickListener(object : TabLayoutExt.OnTabClickListener {
            override fun onTabClicked(position: Int) {
                when (position) {
                    0 -> fillList0()
                    1 -> fillList1()
                    2 -> fillList2()
                }
            }
        })
        (mTabLayout.getChildAt(0) as ViewGroup).getChildAt(2).isEnabled = false
    }
    //endregion

    //region region fillList

    private fun fillList0() {
        currMenu = listMenu0
        if (!this::mId.isInitialized
            && findViewById<EditTextExt>(R.id.txtId) != null) {
            mViewMain0= findViewById(R.id.viewMain0)
            listMenu0 = addMenu()
            orderlineSeqno=1

            contextMenuAdd(SAVE_ID, getText(R.string.save).toString(), R.drawable.save_normal,4,1)
            contextMenuAdd(PRINT_ID, getText(R.string.print).toString(), R.drawable.print_normal,20,1)
            contextMenuAdd(MAIL_ID, getText(R.string.mail).toString(), R.drawable.mail_normal,21,1)
            contextMenuAdd(WHATSAPP_ID, getText(R.string.whatsapp).toString(), R.drawable.whatsapp,22,1)
            // contextMenuAdd(TCPID_ID, getText(R.string.transmit).toString(), R.drawable.tcpip_normal,1)

            mId = findViewById(R.id.txtId)
            ViewUtils.setDBColumn(mId,TableOrder.Columns._id.name)

            mOrderDatum = findViewById(R.id.txtOrderDatum)
            ViewUtils.setDBColumn(mOrderDatum,TableOrder.Columns.orderdate.toString())

            mAfleverDatum = findViewById(R.id.txtAfleverDatum)
            ViewUtils.setDBColumn(mAfleverDatum,TableOrder.Columns.deliverdate.toString())

            mDescription = findViewById(R.id.txtOpmerkingen)
            ViewUtils.setDBColumn(mDescription,TableOrder.Columns.description.toString(), TableOrder.TABLE_NAME, true)

            mNaam = findViewById(R.id.txtNaam)
            ViewUtils.setDBColumn(mNaam,TableOrder.Columns.buyername.toString(), TableOrder.TABLE_NAME, true)

            mAddress = findViewById(R.id.txtAddress)
            ViewUtils.setDBColumn(mAddress,TableOrder.Columns.address.toString(), TableOrder.TABLE_NAME, true)

            mEMail = findViewById(R.id.txtEMail)
            ViewUtils.setDBColumn(mEMail,TableOrder.Columns.email.toString(), TableOrder.TABLE_NAME, true)

            mDiscount = ViewUtils.findMoneyId(mViewMain0, EditTextMoney::class.simpleName!!, R.id.hdrDiscount) as  EditTextMoney

            ViewUtils.setDBColumn(mDiscount,TableOrder.Columns.discount.toString())
            mDiscount.addTextChangedListener(mDiscountTextChangedListener)

            mBedrag = ViewUtils.findMoneyId(mViewMain0, TextViewMoney::class.simpleName!!, R.id.hdrBedrag) as  TextViewMoney
            ViewUtils.setDBColumn(mBedrag,null)

            if (!ConstantsLocal.isPriceUseEnabled) {
                mBedrag.visibility = View.GONE
                mDiscount.visibility = View.GONE
                val tv1: TextViewExt = findViewById(R.id.lblDiscount)
                tv1.visibility = View.GONE
            }
            val map = ContentValuesExt.copyBundleContentValues(intent.extras)
            ViewUtils.copyContentValuesToViewGroup( map,(mViewMain0 as ViewGroup?)!!)

            messDialog?.dismissDialog()

            if (orderId == 0) {
                if (!product.existsProduct("")) {
                    ToastExt().makeText(
                        this, R.string.mess024_No_products,
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                val c = Calendar.getInstance()
                mYear = c[Calendar.YEAR]
                mMonth = c[Calendar.MONTH]
                mDay = c[Calendar.DAY_OF_MONTH]
                mBedrag.textExt = "0"
                mDiscount.textExt = "0"
                mode = 1
            } else {
                val rtn = order.getOrder(orderId)
                if (rtn.cursor.count != 1) {
                    rtn.cursorClose()
                    finish()
                    return
                }
                rtn.cursor.moveToFirst()

                mBedrag.textExt = orderline.getTotalAmount(orderId,0).toString()
                mode = 2
                rtn.cursorClose()
            }
        }
    }

    private fun fillList1() {
        ViewUtils.hideKeyboard(currentFocus, this)
        getTableLayout(0)
        updateTableLayouts()

        if (!this::mTblOrderLines.isInitialized) {

            mTblOrderLines = findViewById(R.id.tblLayout)
            mImage = findViewById(R.id.colPic)
            mColShort = findViewById(R.id.colShort)

            fillList1a()
        }

        fillList1Picture()
    }

    private fun fillList1a() {
        removeAllTableLayoutViews(0)
        val rtn = orderline.getOrderLineIncl(orderId)
        fillTable(rtn.cursor)
        rtn.cursorClose()

        for (i in 0 until mTblOrderLines.childCount) {
            orderlineSeqno=i+1
            val row = mTblOrderLines.getChildAt(i)
            TagModify.setViewTagValue(row,"seqno",orderlineSeqno)
            mImage = row.findViewById(R.id.colPic)
            TagModify.setViewTagValue(row,"seqno",orderlineSeqno)
            rtn.cursorClose()
        }
    }

    private fun fillList1Picture() {
        for (i in 0 until mTblOrderLines.childCount) {
            val row = mTblOrderLines.getChildAt(i)
            mImage = row.findViewById(R.id.colPic)
            val id =
                ViewUtils.getChildValue(row as ViewGroup, TableOrderLine.Columns._productid.name)
                    .toString()
            if (id != "0") {
                val filename = ViewUtils.getChildValue(row, TableProduct.Columns.filename.name)
                val dirname = ViewUtils.getChildValue(row, TableProduct.Columns.dirname.name)
                val rotation =
                    ViewUtils.getChildValue(row, TableProduct.Columns.rotation.name).toString()
                BitmapManager.loadFileToImageView(
                    this,
                    id.toInt(),
                    "$dirname/$filename",
                    50,
                    rotation.toFloat(),
                    mImage
                )
            }
        }
    }

    private fun fillList2() {
        ViewUtils.hideKeyboard(currentFocus, this)
        currMenu = listMenu0
        if (!this::mOms.isInitialized
            && findViewById<TextView>(R.id.txtOms) != null) {
            val vi2: LinearLayout = findViewById(R.id.viewMain2)
            vi2.isEnabled = false
            mOms = findViewById(R.id.txtOms)

            mSubTot = ViewUtils.findMoneyId(vi2, TextViewMoney::class.simpleName!!, R.id.hdrSubTot) as TextViewMoney
            ViewUtils.setDBColumn(mSubTot,null)

            mPrijs = ViewUtils.findMoneyId(vi2, TextViewMoney::class.simpleName!!, R.id.hdrPrijs) as TextViewMoney
            ViewUtils.setDBColumn(mPrijs,TableProduct.Columns.price.toString())

             mCode = findViewById(R.id.txtCode)
            ViewUtils.setDBColumn(mCode,TableProduct.Columns.code.toString())

            mTitle = findViewById(R.id.txtTitle)
            ViewUtils.setDBColumn(mTitle,TableProduct.Columns.title.toString())

            mImgPhoto = findViewById(R.id.imgPhoto)
            ViewUtils.setDBColumn(mImgPhoto,TableOrderLine.Columns._productid.toString())

            btnPhoto = findViewById(R.id.btnPhoto)
            btnPhoto.setOnClickListener(mLocalImgPhotoListener)

            pickerView = findViewById(R.id.picker_view)
            ViewUtils.setDBColumn(pickerView,TableOrderLine.Columns.amount.toString())

            pickerView.setAdapter(simpleAdapter)
            simpleAdapter.values = (0 until 99).map { TextWheelPickerView.Item("$it", "$it") }
            pickerView.isCircular = true
            pickerView.setWheelListener(object : BaseWheelPickerView.WheelPickerViewListener {
                override fun didSelectItem(picker: BaseWheelPickerView, index: Int) {
                    // ("Not yet implemented")
                    val text = TagModify.getViewTagValue(picker, ConstantsFixed.TagSection.TsDBValue.name)
                    if (text.isNotEmpty()) {
                        changedPrijs(index)
                    } else {
                        TagModify.setViewTagValue(picker, ConstantsFixed.TagSection.TsDBValue.name, index)
                    }
                }
            })

            if (!ConstantsLocal.isPriceUseEnabled) {
                val lblText: TextViewExt = findViewById(R.id.lblPrijs)
                lblText.visibility = View.GONE
                val lblBedrag: TextViewExt = findViewById(R.id.lblBedrag)
                lblBedrag.visibility = View.GONE
                val lblDiscount: TextViewExt = findViewById(R.id.lblDiscount)
                lblDiscount.visibility = View.GONE

                mPrijs.visibility = View.GONE
                mDiscount.visibility = View.GONE
                val tv2: TextViewExt = findViewById(R.id.lblSubTot)
                tv2.visibility = View.GONE
                mSubTot.visibility = View.GONE
            }
        }
    }

    private fun fillPhoto(productId: Int) {
        if (productId == 0){
            emptyPhoto()
            return
        }
        val vi2: LinearLayout = findViewById(R.id.viewMain2)
        val rtn = product.getProduct(productId)
        //val vi = getTableRow(currView)!!

        //android:scaleType="fitXY"
        // android:src="@drawable/logo"
        ViewUtils.setChildDBColumnValue(vi2,TableOrderLine.Columns._productid.name, productId.toString())
        //mImgPhoto.tag=TagModify.setTagValue(mImgPhoto.tag,ConstantsFixed.TagSection.TsDBValue.name,productId)
        if (rtn.cursor.count == 1) {
            vi2.isEnabled = true
            if (arrId != null) mPosition = arrId!!.indexOf(productId.toString())

            rtn.cursor.moveToFirst()
            val prijs = Tables.getDouble(rtn.cursor, TableProduct.Columns.price.name)
            mPrijs.textExt = prijs.toString()
            mSubTot.textExt = prijs.toString()
            mCode.text = Tables.getString(rtn.cursor, TableProduct.Columns.code.name)
            mTitle.text = Tables.getString(rtn.cursor, TableProduct.Columns.title.name)
            val rotation = Tables.getFloat(rtn.cursor, TableProduct.Columns.rotation.name)
            mOms.text = Tables.getString(rtn.cursor, TableProduct.Columns.description.name)
            //try {
            fillGraph(productId,
                Tables.getString(rtn.cursor, TableProduct.Columns.dirname.name) ,
                Tables.getString(rtn.cursor, TableProduct.Columns.filename.name), rotation
            )

        } else{
            emptyPhoto()
        }
        rtn.cursorClose()
        changedPrijs(pickerView.selectedIndex)
    }

    private fun emptyPhoto() {
        val vi2: LinearLayout = findViewById(R.id.viewMain2)
        ViewUtils.setChildDBColumnValue(vi2,TableOrderLine.Columns._productid.name, "0")
        mTitle.text = ""
        mOms.text = ""
        mCode.text = ""
        mPrijs.textExt = "0.0"
        mSubTot.textExt = "0.0"
        pickerView.setText("1",true)
        //mImgPhoto.imageAlpha=R.drawable.logo
        vi2.isEnabled = false
        mImgPhoto.fillGraph(0,"emptyPhoto", mImgPhoto.height,0f)
        mImgPhoto.setImageBitmap(BitmapFactory.decodeResource(resources, com.farrusco.projectclasses.R.drawable.logo))
        //mImgPhoto.bitmap = BitmapFactory.decodeResource(resources, com.farrusco.projectclasses.R.drawable.logo)
    }

    private fun fillGraph(id: Int, dirname: String, filename: String, rotation:Float) {
        if (this::mImgPhoto.isInitialized){
            BitmapManager.loadFileToImageView(this, id,"$dirname/$filename", mImgPhoto.height, rotation, mImgPhoto)
        }
    }

    //endregion

    override fun onFragmentAttach(link: String?) {
        if (link != null){
            when (link){
                "f0" -> fillList0()
                "f1" -> fillList1()
                "f2" -> fillList2()
            }
        }
    }

    private val mDiscountTextChangedListener: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            calcTotalPrice()
        }
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
    }

    private fun findRow(seqno: Int): TableRow? {
        for (i in 0 until mTblOrderLines.childCount) {
            val row = mTblOrderLines.getChildAt(i)
            val seqnox = TagModify.getViewTagValue(row,"seqno")
            if (seqnox != "" && seqnox.toInt() == seqno){
                return row as TableRow
            }
        }
        return null
    }

    override fun copyCursorToViewGroup(cursor: Cursor, v: ViewGroup?) {
        super.copyCursorToViewGroup(cursor, v)
        val img = v!!.findViewById<ImageViewExt>(R.id.colPic)
        if (img != null){
            val id = ViewUtils.getValueCursorInt (cursor,TableOrderLine.Columns._productid.name)
            val rotation = ViewUtils.getValueCursorFloat(cursor,TableProduct.Columns.rotation.name)
            val filename = ViewUtils.getValueCursor(cursor,TableProduct.Columns.filename.name)
            val dirname = ViewUtils.getValueCursor(cursor,TableProduct.Columns.dirname.name)
            if (dirname.isNotEmpty() && filename.isNotEmpty()){
                img.fillGraph(id,"$dirname/$filename", ViewUtils.getDip(R.dimen.normal_40sp,this).toInt(), rotation)
            }
        }
    }

    fun changedPrijs(newVal: Int) {
        try {
            var prijs = mPrijs.textExt.toString().toDouble()
            prijs = (newVal * prijs)
            mSubTot.textExt = prijs.toString()

            copyToOrderline()
            calcTotalPrice()
        }catch (_: Exception) {
            // nothing
        }
    }

    private fun calcTotalPrice() {
        mBedrag.textExt = "0"
        if (this::mTblOrderLines.isInitialized){
            var total:Double = 0 - CalcObjects.stringToDouble(mDiscount.textExt.toString())
            for (row in 0 until mTblOrderLines.childCount) {
                val vi = mTblOrderLines.getChildAt(row) as ViewGroup
                val aantal: Int = stringToInteger(ViewUtils.getChildValue(vi, TableOrderLine.Columns.amount.name).toString() )
                val prijs: Double  = CalcObjects.stringToDouble(ViewUtils.getChildValue(vi, TableProduct.Columns.price.name).toString() )
                total += (prijs * aantal)
            }
            mBedrag.textExt = total.toString()
        }
    }

    private fun updateTableLayouts(){
        if (mTblLayout1 == null) {
            mTblLayout1 = findViewById(R.id.tblOrderLines)
            updateTableLayout(R.id.tblLayout1, mTblLayout1)
        }
    }

    private fun copyToOrderline() {

        val vi2 = findRow(orderlineSeqnoCurr) ?: return

        val vi1: LinearLayout = findViewById(R.id.viewMain2)
        ViewUtils.copyViewGroupToView( vi1, vi2 )
    }

    private val mLocalImgPhotoListener = View.OnClickListener {

        intent.setClass(this,SearchFiles::class.java)
        intent.putExtra("parent", "detailordertab")
        intent.putExtra("orderid", orderId.toString())
        intent.putExtra("orderlineid", orderlineId.toString())
        intent.putExtra("blocked", "0")
        intent.putExtra(Constants.RequestCode,"2")
        resultLauncher.launch(intent)
    }

    //region region override

    override fun saveScreen(): ReturnValue {
        var rtn = validateScreen()
        if (!rtn.returnValue) {
            return rtn
        }
        val cv = ViewUtils.copyViewGroupToContentValues((mViewMain0 as ViewGroup))
        cv.keySet().forEach {
            intent.putExtra(it,cv[it].toString())
        }

        if (orderId>0)
            rtn = order.updatePrimaryKey(cv)
        else {
            rtn = order.insertPrimaryKey(cv)
            orderId = rtn.id
            intent.putExtra(TableOrder.Columns._id.name, orderId.toString())
        }
        mId.textExt = orderId.toString()
        if (!rtn.returnValue) {
            return rtn
        }
        ViewUtils.removeChildTag(mViewMain0 as ViewGroup, arrayOf( ConstantsFixed.TagSection.TsUserFlag.name))
        var bFillList1a = false
        for (row in 0 until mTblOrderLines.childCount) {
            val vi = mTblOrderLines.getChildAt(row) as ViewGroup

            val map = ViewUtils.copyViewGroupToContentValues(vi, 0)
            map.put(TableOrderLine.Columns._orderid.name, orderId)
            if (ViewUtils.hasChildTag(vi, ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Delete.name)) {
                if (map.getAsInteger(TableOrderLine.Columns._id.name) > 0) {
                    disableFillList2()
                    rtn = orderline.deletePrimaryKey(map)
                    bFillList1a=true
                }
            } else if (map.getAsInteger(TableOrderLine.Columns._id.name) == 0)  {
                rtn = orderline.insertPrimaryKey(map)
            } else {
                rtn = orderline.updatePrimaryKey(map)
                orderlineId = rtn.id
                ViewUtils.setChildValue(vi,TableOrderLine.Columns._id.name,orderlineId.toString())
            }
            if (!rtn.returnValue) {
                return rtn
            }
        }
        if (bFillList1a){
            // row deleted fill table again
            fillList1a()
        }
        val locIntent = Intent()
        locIntent.putExtra(
            ConstantsFixed.TagSection.TsModFlag.name,
            intent.getStringExtra(ConstantsFixed.TagSection.TsModFlag.name)
        )
        locIntent.removeExtra(ConstantsFixed.TagSection.TsUserFlag.name)
        ViewUtils.copyViewGroupToIntent(mViewMain0 as ViewGroup, locIntent, groupNo )

        ViewUtils.removeChildTag(viewMain as ViewGroup, arrayOf( ConstantsFixed.TagSection.TsUserFlag.name))
        ToastExt().makeText(this, R.string.mess002_saved, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, locIntent)
        return rtn
    }

    override fun validateScreen(): ReturnValue {
        val rtn = super.validateScreen()
        if (!rtn.returnValue) return rtn

        return rtn
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            ConstantsFixed.EDIT_ID -> {
                val vi = ViewUtils.getTableRow(currView)
                if (vi != null &&
                    orderlineId != stringToInteger(ViewUtils.getChildValue(vi,TableOrderLine.Columns._id.name),-1)){
                    pickerView.skipEditTagAlways=true
                    val vi2: LinearLayout = findViewById(R.id.viewMain2)
                    ViewUtils.copyViewGroupToView(vi, vi2)
                    mTabsViewpager.currentItem = 2
                    orderlineSeqnoCurr = TagModify.getViewTagValue(vi,"seqno").toInt()

                    orderlineId = ViewUtils.getChildValue(vi,TableOrderLine.Columns._id.name).toString().toInt()
                    val productId = ViewUtils.getChildValue(vi,TableOrderLine.Columns._productid.name).toString().toInt()
                    fillPhoto(productId)
                    (mTabLayout.getChildAt(0) as ViewGroup).getChildAt(2).isEnabled = true
                    pickerView.skipEditTagAlways=false
                }
                return true
            }
        }
        val ok = super.onContextItemSelected(item)
        calcTotalPrice()
        when (item.itemId) {
            ConstantsFixed.DELETE_ID -> {
                (mTabLayout.getChildAt(0) as ViewGroup).getChildAt(2).isEnabled = false
                orderlineId = -2
            }
        }
        return ok
    }

    private fun disableFillList2(){
        if (orderlineId > 0){
            val vi = ViewUtils.getTableRow(currView)
            if (vi != null &&
                orderlineId == stringToInteger(ViewUtils.getChildValue(vi,TableOrderLine.Columns._id.name),-1)) {
                (mTabLayout.getChildAt(0) as ViewGroup).getChildAt(2).isEnabled = false
                orderlineId = -2
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            ConstantsFixed.ADD_ID -> {
                orderlineId=0
                val bRtn = super.onOptionsItemSelected(item)
                orderlineSeqnoCurr = TagModify.getViewTagValue(currView,"seqno").toInt()
                mTabsViewpager.currentItem = 2
                (mTabLayout.getChildAt(0) as ViewGroup).getChildAt(2).isEnabled = true
                ViewUtils.setChildValue((currView as ViewGroup),TableOrderLine.Columns._id.name, "0")
                emptyPhoto()
                return bRtn
            }
            MAIL_ID -> {
                return createLijst(1)
            }
            PRINT_ID -> {
                return createLijst(2)
            }
            WHATSAPP_ID -> {
                return createLijst(3)
            }
            TCPID_ID -> {
                createXML()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun addTableLine(idx: Int, vi: View, groupno: Int): View {
        val vi1 = super.addTableLine(idx, vi, groupno)
        currView = vi1
        TagModify.setViewTagValue(currView,"seqno",++orderlineSeqno)
        orderlineSeqnoCurr=orderlineSeqno
        return vi1
    }

    override fun resultActivity(result: ActivityResult){
        if (result.resultCode == RESULT_OK && result.data != null) {
            if (bAddressPick){
                if (result.data != null) {
                    buyerId = 0
                    // selectedName rawContactId
                    if (result.data != null) {
                        val selectedName = result.data!!.getStringExtra("selectedName")
                        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                            ContactsContract.Contacts.DISPLAY_NAME +" = \"$selectedName\"",
                            null, null)

                        if (cursor != null && cursor.moveToFirst()) {
                            mNaam.setText("")
                            mEMail.setText("")
                            mAddress.setText("")
                            cursor.columnNames.forEach {
                                val nameIndex = cursor.getColumnIndex(it)
                                when (it){
                                    ContactsContract.Contacts._ID ->
                                        buyerId = cursor.getString(nameIndex).toInt()
                                    ContactsContract.Contacts.DISPLAY_NAME ->
                                        mNaam.setText(cursor.getString(nameIndex))
                                }
                            }
                            val addMail = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.Data.CONTACT_ID + " = $buyerId", null, null)
                            if (addMail != null && addMail.moveToFirst()) {
                                addMail.columnNames.forEach {
                                    val nameIndex = addMail.getColumnIndex(it)
                                    when (it) {
                                        ContactsContract.CommonDataKinds.Email.DATA1 ->
                                            mEMail.setText(addMail.getString(nameIndex))
                                    }
                                }
                                addMail.close()
                            }
                            val addCur = contentResolver.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null, ContactsContract.Data.CONTACT_ID + " = $buyerId", null, null)
                            if (addCur != null && addCur.moveToFirst()) {
                                addCur.columnNames.forEach {
                                    val nameIndex = addCur.getColumnIndex(it)
                                    when (it) {
                                        ContactsContract.CommonDataKinds.StructuredPostal.STREET ->
                                            mAddress.setText(addCur.getString(nameIndex))
                                        ContactsContract.CommonDataKinds.Email.DATA ->
                                            mAddress.setText(addCur.getString(nameIndex))
                                    }
                                }
                                addCur.close()
                            }

                            cursor.close()
                        }
                    }
                }
            } else if (stringToInteger(result.data!!.getStringExtra(Constants.RequestCode),0) == 9) {
                val productId = stringToInteger(result.data!!.getStringExtra(TableProduct.Columns._id.name),0)
                fillPhoto(productId)
                // put image into orderline and mark it to 'edit'
                val vi1 = findRow(orderlineSeqnoCurr) ?: return
                var filename = ""
                var dirname = ""
                var rotation = 0f
                val rtn = product.getProduct(productId)
                if (rtn.cursor.count > 0) {
                    if (rtn.cursor.count > 0) {
                        val map = ContentValues()
                        ViewUtils.copyCursorToContentValues(rtn.cursor,map)
                        if (map.containsKey(TableOrderLine.Columns._id.name)){
                            // rename _id because this is order._id
                            val id = map.getAsInteger(TableOrderLine.Columns._id.name)
                            map.put(TableOrderLine.Columns._productid.name,id)
                            map.remove(TableOrderLine.Columns._id.name)
                        }
                        ViewUtils.copyContentValuesToViewGroup(map,vi1)

                        filename =
                            rtn.cursor.getColumnValueString(TableProduct.Columns.filename.name, "")
                                .toString()
                        dirname =
                            rtn.cursor.getColumnValueString(TableProduct.Columns.dirname.name, "")
                                .toString()
                        rotation =
                            ViewUtils.getValueCursor(rtn.cursor, TableProduct.Columns.rotation.name)
                                .toFloat()
                    }
                }
                rtn.cursorClose()

                ViewUtils.setChildValue(vi1,TableOrderLine.Columns._productid.name,productId.toString())
                ViewUtils.setChildValue(vi1,TableProduct.Columns.filename.name,filename)
                ViewUtils.setChildValue(vi1,TableProduct.Columns.dirname.name,dirname)
                ViewUtils.setChildValue(vi1,TableProduct.Columns.rotation.name,rotation.toString())

                val img = vi1.getChildAt(0) as ImageViewExt
                img.tag = TagModify.setTagValue(
                    img.tag,
                    ConstantsFixed.TagSection.TsUserFlag.name,
                    ConstantsFixed.TagAction.Edit.name
                )
                img.fillGraph(productId,"$dirname/$filename", ViewUtils.getDip(R.dimen.normal_40sp,this).toInt(), rotation)

                val vi2: LinearLayout = findViewById(R.id.viewMain2)
                ViewUtils.setChildValue(vi2,TableOrderLine.Columns._productid.name,productId.toString())
                ViewUtils.setChildValue(vi2,TableProduct.Columns.filename.name,filename)
                ViewUtils.setChildValue(vi2,TableProduct.Columns.dirname.name,dirname)
                ViewUtils.setChildValue(vi2,TableProduct.Columns.rotation.name,rotation.toString())
                return
            }
        }
        bAddressPick=false
    }

    //endregion

    //region region mail/print/export

    private fun sendWhatsApp(): Boolean{

        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, objects.toString())
                //putExtra("jid", "${phone}@s.whatsapp.net")
                type = "text/plain"
                if (ConstantsLocal.isGbWhatsAppEnabled) {
                    setPackage("com.gbwhatsapp")
                } else {
                    setPackage("com.whatsapp")
                }
            }
            val rtn = OrderLine().getOrderLineIncl(orderId)
            if (rtn.cursor.count == 1){
                if (rtn.cursor.moveToFirst()) {
                    val dirname = rtn.cursor.getColumnValueString(TableProduct.Columns.dirname.name)
                    val filename = rtn.cursor.getColumnValueString(TableProduct.Columns.filename.name)
                    val file = File("$dirname/$filename")
                    if (file.exists()) {
                        sendIntent.putExtra(
                            Intent.EXTRA_STREAM,
                            Uri.fromFile(file)
                        )
                    }
                }
            } else if (rtn.cursor.count > 1) {
                sendIntent.action = Intent.ACTION_SEND_MULTIPLE
                val files: ArrayList<Uri?> = ArrayList<Uri?>()
                if (rtn.cursor.moveToFirst()) {
                    do {
                        val dirname =
                            rtn.cursor.getColumnValueString(TableProduct.Columns.dirname.name)
                        val filename =
                            rtn.cursor.getColumnValueString(TableProduct.Columns.filename.name)
                        val file = File("$dirname/$filename")
                        if (file.exists()) {
                            val uri: Uri? = Uri.fromFile(file)
                            files.add(uri)
                        }
                    } while (rtn.cursor.moveToNext())
                    sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                }
            }
            rtn.cursorClose()

            startActivity(sendIntent)
            return true
        }catch (e: Exception){
            e.printStackTrace()

            if (ConstantsLocal.isGbWhatsAppEnabled) {
                startActivity(Intent(Intent.ACTION_VIEW, "https://androidapksfree.com/gb-whatsapp/com-gbwhatsapp".toUri()))
            } else{
                val appPackageName = "com.whatsapp"
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri()))
                } catch (_ :android.content.ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$appPackageName".toUri()))
                }
            }
        }
        return false
    }

    private fun createLijst(typelist: Int): Boolean {
        // 1=mail, 2=print, 3=whatsapp
        val dir = File(Constants.dataPath)
        if (typelist==2){
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    ToastExt().makeText(this, getText(R.string.mess005_nocreatefolder).toString() + dir.path, Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        objects = StringBuffer()
        objects.append(
            (StringUtils.rightPad("" + getText(R.string.order), 10)) + ": $orderId"
        )
        objects.append(
            ("\n"
                    + StringUtils.rightPad("" + getText(R.string.date), 10)) + ": "
                    + mOrderDatum.text
        )
        objects.append("\n")
        val naam = systeem.getValue(SystemAttr.SalesName)
        if (naam.isNotEmpty()) {
            objects.append(
                (("\n"
                        + StringUtils.rightPad("" + getText(R.string.salesman), 10)
                        ) + ": " + naam)
            )
        }
        val address = systeem.getValue(SystemAttr.SalesAddress)
        if (address.isNotEmpty()) {
            objects.append(
                (("\n"
                        + StringUtils.rightPad("" + getText(R.string.street), 10)
                        ) + ": " + address)
            )
            objects.append("\n")
        }
        objects.append(
            (("\n"
                    + StringUtils.rightPad("" + getText(R.string.name), 10)) + ": "
                    + mNaam.text)
        )
        objects.append(
            (("\n"
                    + StringUtils.rightPad("" + getText(R.string.street), 10)
                    ) + ": " + mAddress.text)
        )
        var totaalBedrag = 0.0
        val discount: Double
        var totaalAantal = 0
        if (ConstantsLocal.isPriceUseEnabled) {
            objects.append(
                ("\n\n"
                        + StringUtils.rightPad("" + getText(R.string.code), 10)
                        + StringUtils.rightPad(
                    "" + getText(R.string.description),
                    30
                )
                        + StringUtils.leftPad(getText(R.string.price).toString(), 10)
                        + StringUtils.leftPad(getText(R.string.count).toString(), 7)
                        + StringUtils.leftPad(getText(R.string.amount).toString(), 10))
            )
            objects.append("\n" + StringUtils.repeat("-", 67))
        } else {
            objects.append(
                ("\n\n"
                        + StringUtils.rightPad(getText(R.string.code).toString(), 10)
                        + StringUtils.rightPad(
                    getText(R.string.description).toString(),
                    30
                )
                        + StringUtils.leftPad(getText(R.string.count).toString(), 7))
            )
            objects.append("\n" + StringUtils.repeat("-", 47))
        }


        val rtn = OrderLine().getOrderLineIncl(orderId)
        if (rtn.cursor.moveToFirst()) {
            do {

                val code = rtn.cursor.getColumnValueString(TableProduct.Columns.code.name)
                val description = rtn.cursor.getColumnValueString(TableProduct.Columns.description.name)
                var amount = rtn.cursor.getColumnValueInt(TableOrderLine.Columns.amount.name)
                if (amount == null) amount = 0
                totaalAantal += amount
                if (ConstantsLocal.isPriceUseEnabled) {
                    var price = rtn.cursor.getColumnValueDouble(TableProduct.Columns.price.name)
                    if (price == null) price = 0.0
                    totaalBedrag += price
                    objects.append(
                        ("\n"
                                + StringUtils.rightPad(
                            StringUtils.limit(code, 9), 10
                        )
                                + StringUtils.rightPad(
                            StringUtils.limit(description, 29), 30
                        )
                                + StringUtils.leftPad(
                            CalcObjects.formatAmount(price),
                            10
                        )
                                + StringUtils.leftPad(amount.toString(), 7)
                                + StringUtils.leftPad(
                            CalcObjects.formatAmount(amount*price),
                            10
                        ))
                    )
                } else {
                    objects.append(
                        ("\n"
                                + StringUtils.rightPad(
                            StringUtils.limit(code, 9), 10
                        )
                                + StringUtils.rightPad(
                            StringUtils.limit(description, 29), 30
                        )
                                + StringUtils.leftPad(amount.toString(), 7))
                    )
                }
        } while (rtn.cursor.moveToNext())
    }
    rtn.cursorClose()

    objects.append("\n" + StringUtils.repeat("-", 67))
    objects.append(
        ("\n" + StringUtils.rightPad(getText(R.string.totalsub).toString() + ":",50)
                + StringUtils.leftPad( totaalAantal.toString(), 7)))
        if (ConstantsLocal.isPriceUseEnabled) {
            objects.append(StringUtils.leftPad(CalcObjects.formatAmount((totaalBedrag)), 10))

            objects.append("\n" + StringUtils.repeat("-", 67))
            discount = CalcObjects.stringToDouble(mDiscount.textExt.toString())
            objects.append(("\n" + StringUtils.rightPad(getText(R.string.lbldiscount).toString() + ":",60)
                    + StringUtils.leftPad(CalcObjects.formatAmount(0-discount), 7)))
            objects.append("\n" + StringUtils.repeat("=", 67))

            objects.append("\n" + StringUtils.rightPad(getText(R.string.total).toString() + ":",57) + StringUtils.leftPad(CalcObjects.formatAmount(totaalBedrag-discount), 10))

        }
        if (mDescription.text.toString().isNotEmpty()) {
            objects.append((("\n\n"
                    + StringUtils.rightPad("" + getText(R.string.rem), 8)
                    ) + ": " + mDescription.text.toString())
            )
        }
        rtn.cursorClose()
        when (typelist){
            1 -> {
                sendMailOrder()
            }
            2 -> {
                val file = File(dir, (getText(R.string.order).toString() + "_${orderId}.txt"))
                try {
                    writeToFile(file)
                    ToastExt().makeText(
                        this,
                        (getText(R.string.mess006_listready).toString() +
                                getText(R.string.order).toString() + "_" + orderId.toString() + ".txt"),
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: IOException) {
                    ToastExt().makeText(
                        this,
                        getText(R.string.mess007_listerr).toString() + e.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }
            }
            3 ->{
                sendWhatsApp()
            }
        }
        return true
    }

    @Throws(IOException::class)
    private fun writeToFile(file: File) {
        val fwrite = FileWriter(file, false)
        fwrite.write(this.objects.toString())
        fwrite.close()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun sendMailOrder() {

        val emailTo = mEMail.text.toString()
        val emailCC = systeem.getValue(SystemAttr.SalesEMail)

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailTo))
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT, "Order_$orderId" )
        emailIntent.putExtra(
            Intent.EXTRA_TEXT,
            this.objects.toString()
        )
        emailIntent.putExtra(Intent.EXTRA_CC, arrayOf(emailCC))

        val rtn = OrderLine().getOrderLineIncl(orderId)
        if (rtn.cursor.count == 1){
            if (rtn.cursor.moveToFirst()) {
                val dirname = rtn.cursor.getColumnValueString(TableProduct.Columns.dirname.name)
                val filename = rtn.cursor.getColumnValueString(TableProduct.Columns.filename.name)

                val file = File("$dirname/$filename")
                if (file.exists()){
                    val uri: Uri? = Uri.fromFile(file)
                    emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
                }
             }
        } else if (rtn.cursor.count > 1) {
            intent.action = Intent.ACTION_SEND_MULTIPLE
            val files: ArrayList<Uri?> = ArrayList<Uri?>()
            if (rtn.cursor.moveToFirst()) {
                do {
                    val dirname = rtn.cursor.getColumnValueString(TableProduct.Columns.dirname.name)
                    val filename =
                        rtn.cursor.getColumnValueString(TableProduct.Columns.filename.name)
                    val file = File("$dirname/$filename")
                    if (file.exists()){
                        val uri: Uri? = Uri.fromFile(file)
                        files.add(uri)
                    }
                } while (rtn.cursor.moveToNext())
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
            }
        }
        rtn.cursorClose()

        startActivity(Intent.createChooser(emailIntent, "Send mail..."))
    }

    private fun createXML() {
        // not available (yet)
        objects = StringBuffer()
        objects.append(XmlBuilder.OPEN_XML_STANZA)
        objects.append("<order id=\"" + mId.text + "\">")
        objects.append(" date=\"" + mOrderDatum.text + "\"")
        val naam = systeem.getValue(SystemAttr.SalesName)
        if (naam.isNotEmpty()) {
            objects.append(" salesman=\"$naam\"")
        }
        val address = systeem.getValue(SystemAttr.SalesAddress)
        if (address.isNotEmpty()) {
            objects.append(" sales_address=\"$address\"")
        }
        objects.append(" name=\"" + mNaam.text + "\"")
        var totaalBedrag = java.lang.Double.valueOf(0.0)
        var totaalAantal = 0
        objects.append("<orderlines>")
        val rtn = OrderLine().getOrderLineIncl(orderId)
        while (rtn.cursor.moveToNext()) {
            objects.append("<line" + rtn.cursor.position + ">")
            totaalBedrag += rtn.cursor.getString(3).toString().toDouble()
            totaalAantal += rtn.cursor.getString(1).toString().toInt()
            objects.append(
                (" product=\""
                        + StringUtils.limit(rtn.cursor.getString(5), 9)) + "\""
            )
            objects.append(
                ((" product_name=\""
                        + StringUtils.limit(rtn.cursor.getString(4), 29)) + "\"")
            )
            if (ConstantsLocal.isPriceUseEnabled) {
                objects.append(
                    (" product_price=\""
                            + CalcObjects.formatAmount(rtn.cursor.getString(2)) + "\"")
                )
                objects.append(
                    (" product_amount=\"" + rtn.cursor.getString(1)
                            + "\"")
                )
                objects.append(
                    (" product_total_price=\""
                            + CalcObjects.formatAmount(rtn.cursor.getString(3)) + "\"")
                )
            } else {
                objects.append(
                    ((" product_amount=\""
                            + StringUtils.leftPad(rtn.cursor.getString(1), 7)) + "\"")
                )
            }
            objects.append(" </line" + rtn.cursor.position + ">")
        }
        objects.append(" </orderlines>")
        objects.append(" total=\"$totaalAantal\"")
        if (ConstantsLocal.isPriceUseEnabled) {
            objects.append(
                (" amount=\""
                        + CalcObjects.formatAmount((totaalBedrag)) + "\"")
            )
        }
        if (mDescription.text.toString().isNotEmpty()) {
            objects.append((" remarks=\""
                    + mDescription.text.toString().trim { it <= ' ' } + "\""))
        }
        objects.append(" </order>")
        rtn.cursorClose()
    }

    //endregion
}