package com.dhimandasgupta.funposables.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.funposables.statemachines.CounterState
import com.dhimandasgupta.funposables.statemachines.DecrementAction
import com.dhimandasgupta.funposables.statemachines.IncrementAction
import com.dhimandasgupta.funposables.statemachines.CounterBaseAction
import com.dhimandasgupta.funposables.statemachines.CounterBaseState
import com.dhimandasgupta.funposables.statemachines.SampleStateMachineFactory
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme
import com.freeletics.flowredux2.produceStateMachine

@Composable
fun Counter(modifier: Modifier = Modifier) {
    val factory = retain { SampleStateMachineFactory() }
    val stateMachine = factory.produceStateMachine()

    CounterImplementation(
        modifier = modifier,
        counterBaseState = stateMachine.state.value,
        dispatch = stateMachine.dispatchAction
    )
}

@Composable
private fun CounterImplementation(
    modifier: Modifier = Modifier,
    counterBaseState: CounterBaseState,
    dispatch: (CounterBaseAction) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (counterBaseState) {
                is CounterState -> {
                    Text(
                        modifier = Modifier.padding(all = 16.dp),
                        text = "${counterBaseState.counter}",
                        style = typography.displayLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { dispatch(IncrementAction) },
                            enabled = counterBaseState.enabled
                        ) {
                            Text(text = "Increment")
                        }
                        Button(
                            onClick = { dispatch(DecrementAction) },
                            enabled = counterBaseState.enabled
                        ) {
                            Text(text = "Decrement")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun CounterPreview() {
    FunposablesTheme {
        Counter()
    }
}