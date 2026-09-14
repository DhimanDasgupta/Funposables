package com.dhimandasgupta.funposables.composables.morphingdigit

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

// @formatter:off
val points0 =
  arrayOf(
    9.5711f,
    149.46542f,
    9.4772f,
    86.81623f,
    23.8726f,
    8.74773f,
    91.5763f,
    4.53473f,
    177.6464f,
    -0.82107f,
    202.5491f,
    76.67563f,
    200.7606f,
    153.58434f,
    200.2051f,
    194.06073f,
    190.073f,
    297.1208f,
    100.9805f,
    292.5405f,
    20.6571f,
    288.41107f,
    9.6441f,
    198.09064f,
    9.5711f,
    149.46542f,
  )
val points1 =
  arrayOf(
    70.96325f,
    59.18415f,
    70.96325f,
    59.18415f,
    94.64565f,
    46.46215f,
    105.16455f,
    38.20135f,
    117.79115f,
    28.28515f,
    105.16455f,
    38.20135f,
    139.36595f,
    4.28765f,
    139.36595f,
    4.28765f,
    139.36595f,
    94.72995f,
    139.36595f,
    139.95105f,
    139.36595f,
    190.87135f,
    139.36595f,
    292.71185f,
    139.36595f,
    292.71185f,
  )
val points2 =
  arrayOf(
    20.1064f,
    73.93557f,
    20.0159f,
    34.525f,
    63.4161f,
    9.7031f,
    97.6948f,
    4.9576f,
    126.7435f,
    0.9361f,
    169.1022f,
    15.0676f,
    176.8528f,
    43.35068f,
    203.1914f,
    139.46402f,
    106.1723f,
    210.9431f,
    11.9447f,
    292.72748f,
    95.8437f,
    292.72748f,
    136.0465f,
    292.7245f,
    198.0819f,
    292.71548f,
  )
val points3 =
  arrayOf(
    35.0707f,
    4.27402f,
    35.0707f,
    4.27402f,
    84.0763f,
    4.27402f,
    108.5791f,
    4.27402f,
    134.1587f,
    4.27402f,
    185.318f,
    4.27402f,
    185.318f,
    4.27402f,
    130.7398f,
    77.29711f,
    128.6435f,
    80.20258f,
    103.6807f,
    113.70261f,
    313.538f,
    97.84669f,
    144.3483f,
    418.1831f,
    7.7136f,
    237.02686f,
  )
val points4 =
  arrayOf(
    205.80476f,
    192.59183f,
    205.80476f,
    192.59183f,
    55.25923f,
    192.92033f,
    4.1948476f,
    192.59183f,
    27.430317f,
    159.61295f,
    102.25792f,
    54.636837f,
    138.45938f,
    4.195242f,
    138.45938f,
    53.311993f,
    138.45938f,
    130.2009f,
    138.45938f,
    193.2037f,
    138.45938f,
    224.2571f,
    138.45938f,
    286.36386f,
    138.45938f,
    286.36386f,
  )
val points5 =
  arrayOf(
    173.2887f,
    4.298099f,
    173.2887f,
    4.298099f,
    57.2354f,
    4.298099f,
    36.559902f,
    4.298099f,
    32.553402f,
    39.8558f,
    27.300001f,
    86.4801f,
    22.670002f,
    127.5711f,
    98.0695f,
    59.5335f,
    236.7401f,
    120.7315f,
    192.3053f,
    235.49644f,
    160.0425f,
    315.5381f,
    31.280502f,
    307.4445f,
    9.214203f,
    236.95416f,
  )
val points6 =
  arrayOf(
    180.6085f,
    46.41543f,
    170.477f,
    8.05393f,
    78.6828f,
    -26.9429f,
    37.3649f,
    48.81914f,
    15.5501f,
    93.86806f,
    13.4856f,
    147.64664f,
    19.1209f,
    196.8542f,
    23.999f,
    90.50136f,
    196.8706f,
    87.98967f,
    193.5056f,
    204.21198f,
    190.5619f,
    305.8785f,
    37.5273f,
    340.55817f,
    19.1209f,
    196.8542f,
  )
val points7 =
  arrayOf(
    9.0506f,
    4.28824f,
    9.0506f,
    4.28824f,
    72.4102f,
    4.28824f,
    104.09f,
    4.28824f,
    136.4559f,
    4.28824f,
    171.1942f,
    4.28824f,
    201.1878f,
    4.28824f,
    180.111f,
    48.58288f,
    156.1061f,
    99.03131f,
    133.5652f,
    146.40285f,
    110.3591f,
    195.17249f,
    63.9469f,
    292.71176f,
    63.9469f,
    292.71176f,
  )
val points8 =
  arrayOf(
    103.98236f,
    123.90755f,
    -10.87144f,
    105.65558f,
    -0.2418397f,
    8.938676f,
    100.41516f,
    4.221746f,
    193.70096f,
    -0.149754f,
    235.81526f,
    100.54044f,
    104.01286f,
    123.9475f,
    220.75826f,
    135.92096f,
    245.49376f,
    289.88455f,
    105.21576f,
    292.55386f,
    -30.73964f,
    295.61276f,
    -15.84014f,
    137.13145f,
    104.04336f,
    123.98745f,
  )
val points9 =
  arrayOf(
    193.46806f,
    99.9654f,
    159.02126f,
    243.85802f,
    -13.647492f,
    172.39793f,
    19.290401f,
    68.24112f,
    44.886314f,
    -12.698763f,
    178.54552f,
    -30.17994f,
    193.46806f,
    99.9654f,
    199.40338f,
    151.7295f,
    186.78966f,
    213.13596f,
    177.09557f,
    239.7362f,
    153.55746f,
    311.2439f,
    41.825867f,
    305.8403f,
    28.447966f,
    251.2339f,
  )

// @formatter:on

class TransitionData(points: List<State<Float>>) {
  val p0 by points[0]
  val p1 by points[1]
  val p2 by points[2]
  val p3 by points[3]
  val p4 by points[4]
  val p5 by points[5]
  val p6 by points[6]
  val p7 by points[7]
  val p8 by points[8]
  val p9 by points[9]
  val p10 by points[10]
  val p11 by points[11]
  val p12 by points[12]
  val p13 by points[13]
  val p14 by points[14]
  val p15 by points[15]
  val p16 by points[16]
  val p17 by points[17]
  val p18 by points[18]
  val p19 by points[19]
  val p20 by points[20]
  val p21 by points[21]
  val p22 by points[22]
  val p23 by points[23]
  val p24 by points[24]
  val p25 by points[25]
}

@Composable
fun updateTransitionData(digit: Digit, durationMillis: Int = 500): TransitionData {
  val transition = updateTransition(digit, label = "animated digit points")

  val points = mutableListOf<State<Float>>()
  for (i in 0 until 26) {
    points.add(
      transition.animateFloat(
        label = "p$i",
        transitionSpec = { tween(durationMillis) },
      ) { state ->
        when (state) {
          Digit.ONE -> points1[i]
          Digit.TWO -> points2[i]
          Digit.THREE -> points3[i]
          Digit.FOUR -> points4[i]
          Digit.FIVE -> points5[i]
          Digit.SIX -> points6[i]
          Digit.SEVEN -> points7[i]
          Digit.EIGHT -> points8[i]
          Digit.NINE -> points9[i]
          Digit.ZERO -> points0[i]
        }
      }
    )
  }

  return remember(transition, digit) { TransitionData(points) }
}
