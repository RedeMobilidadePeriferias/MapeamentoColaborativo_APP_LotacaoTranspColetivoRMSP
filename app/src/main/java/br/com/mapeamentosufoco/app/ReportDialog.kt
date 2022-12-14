package br.com.mapeamentosufoco.app

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import br.com.mapeamentosufoco.app.dao.DatabaseHelper
import br.com.mapeamentosufoco.app.entities.Report
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.tiper.MaterialSpinner
import java.text.SimpleDateFormat
import java.util.*

class ReportDialog(private val context: Context, private val dialog: Dialog, private val mMap: GoogleMap) {


    constructor(context: Context, mMap: GoogleMap) : this(
        context,
        Dialog(context, R.style.AppTheme),
        mMap
    )

    private var reportLine: String = ""

    fun startDialog(latLng: LatLng, view : View?) {
        dialog.setContentView(R.layout.add_report)

        initComponents(latLng, view)
    }

    @SuppressLint("SimpleDateFormat")
    fun initComponents(latLng: LatLng, view : View?) {

        val reportTravelReason = dialog.findViewById<MaterialSpinner>(R.id.reportTravelReason)
        val reportTravelTime = dialog.findViewById<MaterialSpinner>(R.id.reportTravelTime)
        val reportTravelStatus = dialog.findViewById<MaterialSpinner>(R.id.reportTravelStatus)
        val groupTravelCategory = dialog.findViewById<RadioGroup>(R.id.groupTravelCategory)

        val appBar = dialog.findViewById<Toolbar>(R.id.toolbar)

        appBar.setOnClickListener {
            dialog.dismiss()
        }

        val reportTravelReasonAdapter = ArrayAdapter(
            context,
            R.layout.materialspinnerlayout,
            context.resources.getStringArray(R.array.arrayTravelReason)
        )
        reportTravelReason.adapter = reportTravelReasonAdapter

        val reportTravelTimeAdapter = ArrayAdapter(
            context,
            R.layout.materialspinnerlayout,
            context.resources.getStringArray(R.array.arrayTravelTime)
        )
        reportTravelTime.adapter = reportTravelTimeAdapter

        val reportTravelStatusAdapter = ArrayAdapter(
            context,
            R.layout.materialspinnerlayout,
            context.resources.getStringArray(R.array.arrayTravelStatus)
        )
        reportTravelStatus.adapter = reportTravelStatusAdapter

        val lableTravelLine = dialog.findViewById<TextView>(R.id.lableTravelLine)
        val reportTravelLine = dialog.findViewById<MaterialSpinner>(R.id.reportTravelLine)
        val reportTextTravelLine = dialog.findViewById<AutoCompleteTextView>(R.id.reportTextTravelLine)
        val lableTravelStation = dialog.findViewById<TextView>(R.id.lableTravelStation)
        val reportTravelStation = dialog.findViewById<MaterialSpinner>(R.id.reportTravelStation)

        val adapter = ArrayAdapter(
            context,
            R.layout.materialspinnerlayout, context.resources.getStringArray(R.array.busLines)
        )
        reportTextTravelLine.setAdapter(adapter)

        val btReport = dialog.findViewById<Button>(R.id.btReport)

        groupTravelCategory.setOnCheckedChangeListener { radioGroup, _ ->
            when (radioGroup.checkedRadioButtonId) {
                R.id.groupTravelCategoryBus -> {
                    reportLine = ""

                    reportTextTravelLine.visibility = TextView.VISIBLE
                    reportTravelLine.visibility = TextView.GONE
                    lableTravelStation.visibility = TextView.GONE
                    reportTravelStation.visibility = TextView.GONE

                }
                R.id.groupTravelCategorySubway -> {
                    val listOfSubwayLines: ArrayList<String> = arrayListOf()

                    listOfSubwayLines.add("Linha 1 - Azul")
                    listOfSubwayLines.add("Linha 2 - Verde")
                    listOfSubwayLines.add("Linha 3 - Vermelha")
                    listOfSubwayLines.add("Linha 4 ??? Amarela")
                    listOfSubwayLines.add("Linha 5 ??? Lil??s")
                    listOfSubwayLines.add("Linha 15 ??? Prata (monotrilho)")

                    val adapterSubway = ArrayAdapter(
                        context,
                        R.layout.materialspinnerlayout,
                        listOfSubwayLines
                    )

                    reportTextTravelLine.visibility = TextView.GONE
                    lableTravelLine.visibility = TextView.VISIBLE
                    reportTravelLine.visibility = TextView.VISIBLE
                    reportTravelLine.adapter = adapterSubway
                }
                else -> {
                    val listOfTrainLines: ArrayList<String> = arrayListOf()

                    listOfTrainLines.add("Linha 7 - Rubi")
                    listOfTrainLines.add("Linha 8 - Diamante")
                    listOfTrainLines.add("Linha 9 - Esmeralda")
                    listOfTrainLines.add("Linha 10 - Turquesa")
                    listOfTrainLines.add("Linha 11 - Coral")
                    listOfTrainLines.add("Linha 12 - Safira")
                    listOfTrainLines.add("Linha 13 - Jade")

                    val adapterTrain = ArrayAdapter(
                        context,
                        R.layout.materialspinnerlayout,
                        listOfTrainLines
                    )

                    reportTextTravelLine.visibility = TextView.GONE
                    lableTravelLine.visibility = TextView.VISIBLE
                    reportTravelLine.visibility = TextView.VISIBLE
                    reportTravelLine.adapter = adapterTrain
                }
            }
        }

        reportTravelLine.onItemSelectedListener = object : MaterialSpinner.OnItemSelectedListener {
            override fun onItemSelected(
                parent: MaterialSpinner,
                view: View?,
                position: Int,
                id: Long
            ) {
                lableTravelStation.visibility = TextView.VISIBLE
                setStationArray(reportTravelLine.selectedItem.toString(), reportTravelStation)
            }

            override fun onNothingSelected(parent: MaterialSpinner) {
                // another interface callback
            }
        }

        btReport.setOnClickListener {

            if (reportTravelReason.selectedItem.toString() !== "null"
                        && reportTravelTime.selectedItem.toString() !== "null"
                        && reportTravelStatus.selectedItem.toString() !== "null"
                && (reportTextTravelLine.visibility == TextView.VISIBLE || reportTravelLine.visibility == TextView.VISIBLE)
            ) {
                val databaseHelper = DatabaseHelper(context)
                val uniqueID = UUID.randomUUID().toString()

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val currentDate = sdf.format(Date())

                val report = Report(
                    0,
                    uniqueID,
                    "${latLng.latitude},${latLng.longitude}",
                    reportTravelReason.selectedItem.toString(),
                    reportTravelTime.selectedItem.toString(),
                    reportTravelStatus.selectedItem.toString(),
                    dialog.findViewById<RadioButton>(groupTravelCategory.checkedRadioButtonId).text.toString(),
                    if (reportLine === "") reportTextTravelLine.text.toString() else reportTravelLine.selectedItem.toString(),
                    if (reportLine === "") reportTextTravelLine.text.toString() else reportLine,
                    currentDate,
                    "N"
                )
                databaseHelper.insertReport(report)

                MapsActivity.addMarker(report, mMap)

                dialog.dismiss()

                Snackbar.make(view!!, "Obrigado por reportar!", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(reportTravelLine, "Por favor, preencha todos os campos!", Snackbar.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }

    private fun setStationArray(line: String, reportTravelStation: MaterialSpinner) {

        reportTravelStation.onItemSelectedListener = object : MaterialSpinner.OnItemSelectedListener {
            override fun onItemSelected(
                parent: MaterialSpinner,
                view: View?,
                position: Int,
                id: Long
            ) {
                reportLine = reportTravelStation.selectedItem.toString()
            }

            override fun onNothingSelected(parent: MaterialSpinner) {
                // another interface callback
            }
        }

        val listOfTrainStation: ArrayList<String> = arrayListOf()

        when (line) {
            "Linha 1 - Azul" -> {
                listOfTrainStation.add("Jabaquara")
                listOfTrainStation.add("Concei????o")
                listOfTrainStation.add("S??o Judas")
                listOfTrainStation.add("Sa??de")
                listOfTrainStation.add("Pra??a da ??rvore")
                listOfTrainStation.add("Santa Cruz")
                listOfTrainStation.add("Vila Mariana")
                listOfTrainStation.add("Ana Rosa")
                listOfTrainStation.add("Para??so")
                listOfTrainStation.add("Vergueiro")
                listOfTrainStation.add("S??o Joaquim")
                listOfTrainStation.add("Jap??o-Liberdade")
                listOfTrainStation.add("S??")
                listOfTrainStation.add("S??o Bento")
                listOfTrainStation.add("Luz")
                listOfTrainStation.add("Tiradentes")
                listOfTrainStation.add("Arm??nia")
                listOfTrainStation.add("Portuguesa-Tiet??")
                listOfTrainStation.add("Carandiru")
                listOfTrainStation.add("Santana")
                listOfTrainStation.add("Jardim S??o Paulo-Ayrton Senna")
                listOfTrainStation.add("Parada Inglesa")
                listOfTrainStation.add("Tucuruvi")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 2 - Verde" -> {
                listOfTrainStation.add("Vila Prudente")
                listOfTrainStation.add("Tamanduate??")
                listOfTrainStation.add("Sacom??")
                listOfTrainStation.add("Alto do Ipiranga")
                listOfTrainStation.add("Santos-Imigrantes")
                listOfTrainStation.add("Ch??cara Klabin")
                listOfTrainStation.add("Ana Rosa")
                listOfTrainStation.add("Para??so")
                listOfTrainStation.add("Brigadeiro")
                listOfTrainStation.add("Trianon-Masp")
                listOfTrainStation.add("Consola????o")
                listOfTrainStation.add("Cl??nicas")
                listOfTrainStation.add("Santu??rio N.S. de F??tima-Sumar??")
                listOfTrainStation.add("Vila Madalena")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 3 - Vermelha" -> {
                listOfTrainStation.add("Corinthians-Itaquera")
                listOfTrainStation.add("Artur Alvim")
                listOfTrainStation.add("Patriarca")
                listOfTrainStation.add("Guilhermina-Esperan??a")
                listOfTrainStation.add("Vila Matilde")
                listOfTrainStation.add("Penha")
                listOfTrainStation.add("Carr??o")
                listOfTrainStation.add("Tatuap??")
                listOfTrainStation.add("Bel??m")
                listOfTrainStation.add("Bresser-Mo??ca")
                listOfTrainStation.add("Br??s")
                listOfTrainStation.add("Pedro II")
                listOfTrainStation.add("S??")
                listOfTrainStation.add("Anhangaba??")
                listOfTrainStation.add("Rep??blica")
                listOfTrainStation.add("Santa Cec??lia")
                listOfTrainStation.add("Marechal Deodoro")
                listOfTrainStation.add("Palmeiras-Barra Funda")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 4 ??? Amarela" -> {
                listOfTrainStation.add("S??o Paulo - Morumbi")
                listOfTrainStation.add("Butant??")
                listOfTrainStation.add("Pinheiros")
                listOfTrainStation.add("Faria Lima")
                listOfTrainStation.add("Fradique Coutinho")
                listOfTrainStation.add("Oscar Freire")
                listOfTrainStation.add("Paulista")
                listOfTrainStation.add("Higien??polis - Mackenzie")
                listOfTrainStation.add("Rep??blica")
                listOfTrainStation.add("Luz")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 5 ??? Lil??s" -> {
                listOfTrainStation.add("Cap??o Redondo")
                listOfTrainStation.add("Campo Limpo")
                listOfTrainStation.add("Vila das Belezas")
                listOfTrainStation.add("Giovanni Gronchi")
                listOfTrainStation.add("Santo Amaro")
                listOfTrainStation.add("Largo Treze")
                listOfTrainStation.add("Adolfo Pinheiro")
                listOfTrainStation.add("Alto da Boa Vista")
                listOfTrainStation.add("Borba Gato")
                listOfTrainStation.add("Brooklin")
                listOfTrainStation.add("Campo Belo")
                listOfTrainStation.add("Eucaliptos")
                listOfTrainStation.add("Moema")
                listOfTrainStation.add("AACD - Servidor")
                listOfTrainStation.add("Hospital S??o Paulo")
                listOfTrainStation.add("Santa Cruz")
                listOfTrainStation.add("Ch??cara Kablin")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 15 ??? Prata (monotrilho)" -> {
                listOfTrainStation.add("Vila Prudente")
                listOfTrainStation.add("Orat??rio")
                listOfTrainStation.add("S??o Lucas")
                listOfTrainStation.add("Camilo Haddad")
                listOfTrainStation.add("Vila Tolst??i")
                listOfTrainStation.add("Vila Uni??o")
                listOfTrainStation.add("Jardim Planalto")
                listOfTrainStation.add("Sapopemba")
                listOfTrainStation.add("Fazenda da Juta")
                listOfTrainStation.add("S??o Mateus")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 7 - Rubi" -> {
                listOfTrainStation.add("Jundia??")
                listOfTrainStation.add("V??rzea Paulista")
                listOfTrainStation.add("Campo Limpo Paulista")
                listOfTrainStation.add("Botujuru")
                listOfTrainStation.add("Francisco Morato")
                listOfTrainStation.add("Baltazar Fid??lis")
                listOfTrainStation.add("Franco Da Rocha")
                listOfTrainStation.add("Caieiras")
                listOfTrainStation.add("Perus")
                listOfTrainStation.add("Vila Aurora")
                listOfTrainStation.add("Jaragu??")
                listOfTrainStation.add("Vila Clarice")
                listOfTrainStation.add("Pirituba")
                listOfTrainStation.add("Piqueri")
                listOfTrainStation.add("Lapa")
                listOfTrainStation.add("??gua Branca")
                listOfTrainStation.add("Jundia??")
                listOfTrainStation.add("Palmeiras-Barra Funda")
                listOfTrainStation.add("Luz")
                listOfTrainStation.add("Br??s")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 8 - Diamante" -> {
                listOfTrainStation.add("J??lio Prestes")
                listOfTrainStation.add("Palmeiras-Barra Funda")
                listOfTrainStation.add("Lapa")
                listOfTrainStation.add("Domingos De Moraes")
                listOfTrainStation.add("Imperatriz Leopoldina")
                listOfTrainStation.add("Presidente Altino")
                listOfTrainStation.add("Osasco")
                listOfTrainStation.add("Comandante Sampaio")
                listOfTrainStation.add("Quita??na")
                listOfTrainStation.add("General Miguel Costa")
                listOfTrainStation.add("Carapicu??ba")
                listOfTrainStation.add("Santa Terezinha")
                listOfTrainStation.add("Antonio Jo??o")
                listOfTrainStation.add("Barueri")
                listOfTrainStation.add("Jardim Belval")
                listOfTrainStation.add("Jardim Silveira")
                listOfTrainStation.add("Jandira")
                listOfTrainStation.add("Sagrado Cora????o")
                listOfTrainStation.add("Engenheiro Cardoso")
                listOfTrainStation.add("Itapevi")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 9 - Esmeralda" -> {
                listOfTrainStation.add("Osasco")
                listOfTrainStation.add("Presidente Altino")
                listOfTrainStation.add("Ceasa")
                listOfTrainStation.add("Villa Lobos - Jaguar??")
                listOfTrainStation.add("Cidade Universit??ria")
                listOfTrainStation.add("Pinheiros")
                listOfTrainStation.add("Rebou??as Hebraica")
                listOfTrainStation.add("Cidade Jardim")
                listOfTrainStation.add("Vila Ol??mpia")
                listOfTrainStation.add("Berrini")
                listOfTrainStation.add("Morumbi")
                listOfTrainStation.add("Granja Julieta")
                listOfTrainStation.add("Santo Amaro")
                listOfTrainStation.add("Socorro")
                listOfTrainStation.add("Jurubatuba")
                listOfTrainStation.add("Aut??dromo")
                listOfTrainStation.add("Interlagos")
                listOfTrainStation.add("Graja??")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 10 - Turquesa" -> {
                listOfTrainStation.add("Br??s")
                listOfTrainStation.add("Luz")
                listOfTrainStation.add("Mooca")
                listOfTrainStation.add("Ipiranga")
                listOfTrainStation.add("Tamanduate??")
                listOfTrainStation.add("S??o Cateano")
                listOfTrainStation.add("Utinga")
                listOfTrainStation.add("Prefeito Saladino")
                listOfTrainStation.add("Santo Andr?? - Prefeito Celso Daniel")
                listOfTrainStation.add("Capuava")
                listOfTrainStation.add("Mau??")
                listOfTrainStation.add("Guapituba")
                listOfTrainStation.add("Ribeir??o Pires")
                listOfTrainStation.add("Rio Grande da Serra")
                listOfTrainStation.add("Paranapiacaba")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 11 - Coral" -> {
                listOfTrainStation.add("Luz")
                listOfTrainStation.add("Br??s")
                listOfTrainStation.add("Tatuap??")
                listOfTrainStation.add("Corinthians-Itaquera")
                listOfTrainStation.add("Dom Bosco")
                listOfTrainStation.add("Jos?? Bonif??cio")
                listOfTrainStation.add("Guaianases")
                listOfTrainStation.add("Ant??nio Gianetti Neto")
                listOfTrainStation.add("Ferraz de Vasconcelos")
                listOfTrainStation.add("Po??")
                listOfTrainStation.add("Calmon Viana")
                listOfTrainStation.add("Suzano")
                listOfTrainStation.add("Jundiapeba")
                listOfTrainStation.add("Br??s Cubas")
                listOfTrainStation.add("Mogi das Cruzes")
                listOfTrainStation.add("Estudantes")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 12 - Safira" -> {
                listOfTrainStation.add("Br??s")
                listOfTrainStation.add("Tatuap??")
                listOfTrainStation.add("Engenheiro Goulart")
                listOfTrainStation.add("USP Leste")
                listOfTrainStation.add("Comendador Ermelino")
                listOfTrainStation.add("S??o Miguel Paulista")
                listOfTrainStation.add("Jardim Helena - Vila Mara")
                listOfTrainStation.add("Itaim Paulista")
                listOfTrainStation.add("Jardim Romano")
                listOfTrainStation.add("Engenheiro Manoel Feio")
                listOfTrainStation.add("Itaquaquecetuba")
                listOfTrainStation.add("Aracar??")
                listOfTrainStation.add("Calmon Viana")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }
            "Linha 13 - Jade" -> {
                listOfTrainStation.add("Aeroporto-Guarulhos")
                listOfTrainStation.add("Guarulhos-Cecap")
                listOfTrainStation.add("Engenheiro Goulart")

                val adapterStation = ArrayAdapter(
                    context,
                    R.layout.materialspinnerlayout,
                    listOfTrainStation
                )

                reportTravelStation.visibility = TextView.VISIBLE
                reportTravelStation.adapter = adapterStation
            }

            else -> {
            }
        }
    }
}
