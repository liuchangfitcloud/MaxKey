/*
 * Copyright [2022] [MaxKey of copyright http://www.maxkey.top]
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

import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, Inject, OnDestroy, Optional } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { environment } from '@env/environment';
import { NzMessageService } from 'ng-zorro-antd/message';

import { Accounts } from '../../../entity/Accounts';
import { AccountsService } from '../../../service/accounts.service';
@Component({
  selector: 'app-credential',
  templateUrl: './credential.component.html',
  styleUrls: ['./credential.component.less']
})
export class CredentialComponent implements OnInit {
  form: {
    submitting: boolean;
    model: Accounts;
  } = {
      submitting: false,
      model: new Accounts()
    };
  redirect_uri: string = '';
  formGroup: FormGroup = new FormGroup({});

  confirmPasswordVisible = false;

  passwordVisible = false;

  constructor(
    fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private accountsService: AccountsService,
    private msg: NzMessageService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.accountsService.get(this.route.snapshot.queryParams['appId']).subscribe(res => {
      console.log(res.data);
      this.form.model.init(res.data);
      this.cdr.detectChanges();
    });

    this.redirect_uri = this.route.snapshot.queryParams['redirect_uri'];
  }

  onSubmit(): void {
    this.form.submitting = true;
    this.form.model.trans();
    //if (this.formGroup.valid) {
    this.accountsService.update(this.form.model).subscribe(res => {
      if (res.code == 0) {
        this.form.model.init(res.data);
        this.msg.success(`提交成功`);
        if (this.redirect_uri) {
          window.location.href = `${environment.api.baseUrl}${this.redirect_uri}`;
        }
      } else {
        this.msg.success(`提交失败`);
      }
    });
    // } else {
    //  this.formGroup.updateValueAndValidity({ onlySelf: true });
    // this.msg.success(`提交失败`);
    //}
    this.form.submitting = false;
    this.cdr.detectChanges();
  }
}
