package com.example.calculator_project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color


val buttonList = listOf(
    "(",")","C","⌫",
    "7","8","9","÷",
    "4","5","6","x",
    "1","2","3","-",
    "=","0",".","+"
)
@Composable
fun Calculator(modifier: Modifier = Modifier,viewModel: CalculatorViewModel ) {

    val equationText = viewModel.equationText.observeAsState()
    val resultText = viewModel.resulText.observeAsState()

    Box(modifier = modifier){
        Column(
            modifier=modifier.fillMaxSize(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = equationText.value?:"",
                style = TextStyle(
                    fontSize = 30.sp,
                    textAlign = TextAlign.End
                ),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = resultText.value?:"" ,
                style = TextStyle(
                    fontSize = 60.sp,
                    textAlign = TextAlign.End
                ),
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
            ) {
                items(buttonList){
                    CalculatorButton(btn = it, onClick = {
                        viewModel.onButtonClick(it)
                    })

                }
            }

        }
    }

}

@Composable
fun CalculatorButton(btn : String,onClick : ()-> Unit) {
    Box(modifier = Modifier.padding(10.dp)){
        FloatingActionButton(
            onClick =  onClick,
            modifier = Modifier.size(80.dp),
            contentColor = getColor1(btn),
            containerColor = getColor(btn)
        ){
            Text(
                text = btn,
                fontSize = 30.sp)
        }
    }

}

fun getColor(btn : String) : Color{
    if(btn == "⌫" || btn == "C")
        return Color(0xFFD00000)
    if(btn == "(" || btn == ")")
        return Color.White
    if (btn == "÷" || btn == "x" || btn == "+" || btn == "-" ||btn == "=")
        return Color(0xFF000000)
    return Color(0xFFFFFFFF)
}
fun getColor1(btn : String) :Color{
    if(btn == "⌫" || btn == "C")
        return Color(0xFFFFFFFF)
    if (btn == "÷" || btn == "x" || btn == "+" || btn == "-" ||btn == "=")
        return Color(0xFFFFFFFF)
    return Color(0xFF000000)
}