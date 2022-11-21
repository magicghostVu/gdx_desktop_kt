package pack.logic.team_id

data class PlayerRotateData(val teamId: TeamId, var rotateState: PlayerRotateState = PlayerRotateState.NORMAL) {
}