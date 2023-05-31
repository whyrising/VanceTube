package com.github.yahyatinani.tubeyou.modules.panel.common.search

import com.github.whyrising.recompose.cofx.injectCofx
import com.github.whyrising.recompose.fx.BuiltInFx.fx
import com.github.whyrising.recompose.ids.recompose.db
import com.github.whyrising.recompose.regEventDb
import com.github.whyrising.recompose.regEventFx
import com.github.whyrising.y.core.collections.PersistentVector
import com.github.whyrising.y.core.get
import com.github.whyrising.y.core.m
import com.github.whyrising.y.core.v
import com.github.yahyatinani.tubeyou.modules.core.keywords.common.api_url
import com.github.yahyatinani.tubeyou.modules.core.keywords.common.is_search_bar_active
import com.github.yahyatinani.tubeyou.modules.core.keywords.search
import com.github.yahyatinani.tubeyou.modules.core.keywords.search.get_search_results
import com.github.yahyatinani.tubeyou.modules.core.keywords.search.get_search_suggestions
import com.github.yahyatinani.tubeyou.modules.core.keywords.search.set_search_results
import com.github.yahyatinani.tubeyou.modules.core.keywords.search.set_suggestions
import com.github.yahyatinani.tubeyou.modules.panel.common.AppDb
import com.github.yahyatinani.tubeyou.modules.panel.common.activeTab
import com.github.yahyatinani.tubeyou.modules.panel.common.appDbBy
import com.github.yahyatinani.tubeyou.modules.panel.common.ktor
import com.github.yahyatinani.tubeyou.modules.panel.common.trigger
import io.ktor.http.HttpMethod
import io.ktor.util.reflect.typeInfo

fun regCommonEvents() {
  regEventDb<AppDb>(id = is_search_bar_active) { db, (_, flag) ->
    db.assoc(is_search_bar_active, flag)
  }

  regEventFx(
    id = get_search_suggestions,
    interceptors = v(injectCofx(search.coroutine_scope))
  ) { cofx, (_, searchQuery) ->
    val sq = (searchQuery as String).replace(" ", "%20")
    val appDb = appDbBy(cofx)
    val suggestionsEndpoint = "${appDb[api_url]}/suggestions?query=$sq"

    m<Any, Any>(
      fx to v(
        v(
          ktor.http_fx,
          m(
            ktor.method to HttpMethod.Get,
            ktor.url to suggestionsEndpoint,
            ktor.timeout to 8000,
            ktor.coroutine_scope to cofx[search.coroutine_scope],
            ktor.response_type_info to typeInfo<PersistentVector<String>>(),
            ktor.on_success to v(search.fsm, set_suggestions, searchQuery),
            ktor.on_failure to v(":search/error")
          )
        )
      )
    )
  }

  regEventFx(
    id = get_search_results,
    interceptors = v(injectCofx(search.coroutine_scope))
  ) { cofx, (_, sq) ->
    val handleResultsEvent = v(search.fsm, set_search_results)
    m<Any, Any>(
      fx to v(
        v(
          ktor.http_fx,
          m(
            ktor.method to HttpMethod.Get,
            ktor.url to "${appDbBy(cofx)[api_url]}/search?q=$sq&filter=all",
            ktor.timeout to 8000,
            ktor.coroutine_scope to cofx[search.coroutine_scope],
            ktor.response_type_info to typeInfo<SearchResponse>(),
            ktor.on_success to handleResultsEvent,
            ktor.on_failure to handleResultsEvent
          )
        )
      )
    )
  }

  regEventFx(id = search.fsm) { cofx, e ->
    val appDb = appDbBy(cofx)
    trigger(
      searchMachine,
      m(db to appDb),
      v(activeTab(appDb), search.sb_state),
      e.subvec(1, e.count)
    )
  }
}
